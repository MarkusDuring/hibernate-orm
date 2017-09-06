/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

import org.hibernate.HibernateException;
import org.hibernate.query.criteria.spi.JpaCriteriaBuilderImplementor;
import org.hibernate.query.criteria.spi.JpaExpressionImplementor;
import org.hibernate.query.criteria.spi.JpaPredicateImplementor;
import org.hibernate.query.sqm.QueryException;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.produce.spi.criteria.JpaExpression;
import org.hibernate.query.sqm.tree.SqmParameter;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.SqmStatement;
import org.hibernate.query.sqm.tree.expression.LiteralBigDecimalSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralBigIntegerSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralCharacterSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralDoubleSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralFalseSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralFloatSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralIntegerSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralLongSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralStringSqmExpression;
import org.hibernate.query.sqm.tree.expression.LiteralTrueSqmExpression;
import org.hibernate.query.sqm.tree.expression.NamedParameterSqmExpression;
import org.hibernate.query.sqm.tree.expression.PositionalParameterSqmExpression;
import org.hibernate.query.sqm.SemanticException;
import org.hibernate.query.sqm.tree.expression.SubQuerySqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.predicate.AndSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;
import org.hibernate.query.sqm.tree.select.SqmSelection;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.produce.metamodel.spi.EntityValuedExpressableType;
import org.hibernate.type.spi.BasicType;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 * @author Christian Beikov
 */
public abstract class AbstractSqmStatement implements SqmStatement, AbstractQuery, ParameterCollector {
	private final JpaCriteriaBuilderImplementor criteriaBuilder;
	private final ParsingContext parsingContext;
	private Map<String,NamedParameterSqmExpression> namedQueryParameters;
	private Map<Integer,PositionalParameterSqmExpression> positionalQueryParameters;

	public AbstractSqmStatement(
			JpaCriteriaBuilderImplementor criteriaBuilder,
			ParsingContext parsingContext) {
		this.criteriaBuilder = criteriaBuilder;
		this.parsingContext = parsingContext;
	}

	@Override
	public void addParameter(NamedParameterSqmExpression parameter) {
		assert parameter.getName() != null;
		assert parameter.getPosition() == null;

		if ( namedQueryParameters == null ) {
			// TODO: discuss why we need a concurrent hash map here and below
			namedQueryParameters = new ConcurrentHashMap<>();
		}

		namedQueryParameters.put( parameter.getName(), parameter );
	}

	@Override
	public void addParameter(PositionalParameterSqmExpression parameter) {
		assert parameter.getPosition() != null;
		assert parameter.getName() == null;

		if ( positionalQueryParameters == null ) {
			positionalQueryParameters = new ConcurrentHashMap<>();
		}

		positionalQueryParameters.put( parameter.getPosition(), parameter );
	}

	public void wrapUp() {
		validateParameters();
	}

	private void validateParameters() {
		if ( positionalQueryParameters == null ) {
			return;
		}

		// validate the positions.  JPA says that these should start with 1 and
		// increment contiguously (no gaps)
		int[] positionsArray = positionalQueryParameters.keySet().stream().mapToInt( Integer::intValue ).toArray();
		Arrays.sort( positionsArray );

		int previous = 0;
		for ( Integer position : positionsArray ) {
			if ( position != previous + 1 ) {
				if ( previous == 0 ) {
					throw new SemanticException( "Positional parameters did not start with 1 : " + position );
				}
				else {
					throw new SemanticException( "Gap in positional parameter positions; skipped " + (previous+1) );
				}
			}
			previous = position;
		}
	}

	@Override
	public Set<SqmParameter> getQueryParameters() {
		Set<SqmParameter> parameters = new HashSet<>();
		if ( namedQueryParameters != null ) {
			parameters.addAll( namedQueryParameters.values() );
		}
		if ( positionalQueryParameters != null ) {
			parameters.addAll( positionalQueryParameters.values() );
		}
		return parameters;
	}

	@Override
	public JpaCriteriaBuilderImplementor getCriteriaBuilder() {
		return criteriaBuilder;
	}

	@Override
	public <U> Subquery<U> subquery(Class<U> type) {
		TypeConfiguration typeConfiguration = parsingContext.getSessionFactory().getTypeConfiguration();
		BasicType<U> basicType = typeConfiguration.getBasicTypeRegistry().getBasicType( type );
		return new SubQuerySqmExpression( criteriaBuilder, parsingContext, this, new SqmQuerySpec(), basicType );
	}

	protected final SqmRoot createRoot(EntityType<?> entityType) {
		return createRootInternal( entityType.getJavaType() );
	}

	protected final SqmRoot createRoot(Class<?> entityClass) {
		try {
			return createRootInternal( entityClass );
		} catch (HibernateException ex) {
			throw new IllegalArgumentException( "Not an entity type : " + entityClass.getName(), ex );
		}
	}

	private final SqmRoot createRootInternal(Class<?> entityClass) {
		EntityValuedExpressableType<?> entityReference = criteriaBuilder.getSessionFactory()
				.getTypeConfiguration()
				.resolveEntityReference( entityClass );
		String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		return new SqmRoot( null, parsingContext.makeUniqueIdentifier(), alias, entityReference );
	}

	protected final SqmNavigableReference getAttributeReference(SqmRoot root, String attributePath) {
		return parsingContext.findOrCreateNavigableBinding(
				root.getNavigableReference(),
				attributePath
		);
	}

	protected final SqmSelection createSelection(Expression expression) {
		return new SqmSelection( ((JpaExpressionImplementor) expression).getSqmExpression() );
	}

	protected final void setWhere(SqmWhereClause whereClause, Expression restriction) {
		whereClause.setPredicate( ( (JpaPredicateImplementor) restriction ).getSqmPredicate() );
	}

	protected final void setWhere(SqmWhereClause whereClause, Predicate... restrictions) {
		if (restrictions.length == 0) {
			whereClause.setPredicate( null );
			return;
		}

		SqmPredicate rhs = ( (JpaPredicateImplementor) restrictions[restrictions.length - 1] ).getSqmPredicate();
		for (int i = restrictions.length - 2; i >= 0; i--) {
			SqmPredicate lhs = ( (JpaPredicateImplementor) restrictions[i] ).getSqmPredicate();
			rhs = new AndSqmPredicate( lhs, rhs );
		}

		whereClause.setPredicate( rhs );
	}

	@SuppressWarnings("unchecked")
	protected final <T> LiteralSqmExpression<T> createConstantExpression(T value) {
		if ( value == null ) {
			throw new NullPointerException( "Value passed as `constant value` cannot be null" );
		}

		return createConstantExpression( value, (Class<T>) value.getClass() );
	}

	@SuppressWarnings("unchecked")
	protected final <T> LiteralSqmExpression<T> createConstantExpression(T value, Class<T> javaType) {
		if ( Boolean.class.isAssignableFrom( javaType ) ) {
			if ( (Boolean) value ) {
				return (LiteralSqmExpression<T>) new LiteralTrueSqmExpression(
						resolveBasicExpressionType( Boolean.class )
				);
			}
			else {
				return (LiteralSqmExpression<T>) new LiteralFalseSqmExpression(
						resolveBasicExpressionType( Boolean.class )
				);
			}
		}
		else if ( Integer.class.isAssignableFrom( javaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralIntegerSqmExpression(
					(Integer) value,
					resolveBasicExpressionType( Integer.class )
			);
		}
		else if ( Long.class.isAssignableFrom( javaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralLongSqmExpression(
					(Long) value,
					resolveBasicExpressionType( Long.class )
			);
		}
		else if ( Float.class.isAssignableFrom( javaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralFloatSqmExpression(
					(Float) value,
					resolveBasicExpressionType( Float.class )
			);
		}
		else if ( Double.class.isAssignableFrom( javaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralDoubleSqmExpression(
					(Double) value,
					resolveBasicExpressionType( Double.class )
			);
		}
		else if ( BigInteger.class.isAssignableFrom( javaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralBigIntegerSqmExpression(
					(BigInteger) value,
					resolveBasicExpressionType( BigInteger.class )
			);
		}
		else if ( BigDecimal.class.isAssignableFrom( javaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralBigDecimalSqmExpression(
					(BigDecimal) value,
					resolveBasicExpressionType( BigDecimal.class )

			);
		}
		else if ( Character.class.isAssignableFrom( javaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralCharacterSqmExpression(
					(Character) value,
					resolveBasicExpressionType( Character.class )
			);
		}
		else if ( String.class.isAssignableFrom( javaType ) ) {
			return (LiteralSqmExpression<T>) new LiteralStringSqmExpression(
					(String) value,
					resolveBasicExpressionType( String.class )
			);
		}

		throw new QueryException(
				"Unexpected literal expression [value=" + value +
						", javaType=" + javaType.getName() +
						"]; expecting boolean, int, long, float, double, BigInteger, BigDecimal, char, or String"
		);
	}

	private <T> BasicValuedExpressableType<T> resolveBasicExpressionType(Class<T> typeClass) {
		return parsingContext.getSessionFactory().getTypeConfiguration().getBasicTypeRegistry().getBasicType( typeClass );
	}
}
