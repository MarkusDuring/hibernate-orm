/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal;

import org.hibernate.QueryException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.AllowableFunctionReturnType;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.expression.*;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;
import org.hibernate.query.sqm.tree.expression.function.*;
import org.hibernate.query.sqm.tree.from.SqmFromClause;
import org.hibernate.query.sqm.tree.group.SqmGroupByClause;
import org.hibernate.query.sqm.tree.internal.SqmDeleteStatementImpl;
import org.hibernate.query.sqm.tree.internal.SqmSelectStatementImpl;
import org.hibernate.query.sqm.tree.internal.SqmUpdateStatementImpl;
import org.hibernate.query.sqm.tree.order.SqmOrderByClause;
import org.hibernate.query.sqm.tree.order.SqmSortOrder;
import org.hibernate.query.sqm.tree.order.SqmSortSpecification;
import org.hibernate.query.sqm.tree.paging.SqmLimitOffsetClause;
import org.hibernate.query.sqm.tree.predicate.*;
import org.hibernate.query.sqm.tree.select.SqmDynamicInstantiation;
import org.hibernate.query.sqm.tree.select.SqmSelectClause;
import org.hibernate.query.sqm.tree.select.SqmSelectionBase;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.tree.spi.TrimSpecification;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.type.spi.BasicType;
import org.hibernate.type.spi.BasicTypeRegistry;

import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 *
 * @author Christian Beikov
 */
@SuppressWarnings({ "unchecked" })
public class CriteriaBuilderImpl implements CriteriaBuilderImplementor {

	private final SessionFactoryImplementor sessionFactory;

	private final BasicType<Boolean> booleanType;
	private final BasicType<Integer> integerType;
	private final BasicType<Long> longType;
	private final BasicType<Double> doubleType;
	private final BasicType<String> stringType;

	public CriteriaBuilderImpl(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
		BasicTypeRegistry basicTypeRegistry = sessionFactory.getTypeConfiguration().getBasicTypeRegistry();
		this.booleanType = basicTypeRegistry.getBasicType( Boolean.class );
		this.integerType = basicTypeRegistry.getBasicType( Integer.class );
		this.longType = basicTypeRegistry.getBasicType( Long.class );
		this.doubleType = basicTypeRegistry.getBasicType( Double.class );
		this.stringType = basicTypeRegistry.getBasicType( String.class );
	}

	@Override
	public void close() {
	}

	@Override
	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public Predicate wrap(Expression<Boolean> expression) {
		return isTrue( expression );
	}

	@Override
	public CriteriaQuery<Object> createQuery() {
		return createQuery( Object.class );
	}

	@Override
	public <T> CriteriaQuery<T> createQuery(Class<T> resultClass) {
		SqmCreationContext creationContext = CriteriaQueryCreationContext.forQuery( sessionFactory );
		return new SqmSelectStatementImpl<>(
				creationContext,
				resultClass,
				new SqmQuerySpec(
						new SqmFromClause(),
						new SqmSelectClause(false, resultClass),
						new SqmWhereClause(),
                        new SqmGroupByClause(),
						new SqmHavingClause(),
						new SqmOrderByClause(),
						new SqmLimitOffsetClause()
				)
		);
	}

	@Override
	public CriteriaQuery<Tuple> createTupleQuery() {
		return createQuery( Tuple.class );
	}

	@Override
	public <T> CriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity) {
		SqmCreationContext creationContext = CriteriaQueryCreationContext.forDml( sessionFactory );
		return new SqmUpdateStatementImpl(
				creationContext,
				targetEntity
		);
	}

	@Override
	public <T> CriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity) {
		SqmCreationContext creationContext = CriteriaQueryCreationContext.forDml( sessionFactory );
		return new SqmDeleteStatementImpl(
				creationContext,
				targetEntity
		);
	}

	@Override
	public <Y> CompoundSelection<Y> construct(Class<Y> resultClass, Selection<?>... selections) {
		JavaTypeDescriptor<Y> javaTypeDescriptor = sessionFactory.getTypeConfiguration()
				.getJavaTypeDescriptorRegistry()
				.getDescriptor( resultClass );
		SqmDynamicInstantiation dynamicInstantiation;
		if ( resultClass == Map.Entry.class ) {
			dynamicInstantiation = SqmDynamicInstantiation.forMapInstantiation( (JavaTypeDescriptor<Map>) javaTypeDescriptor );
		} else if ( resultClass == List.class ) {
			dynamicInstantiation = SqmDynamicInstantiation.forListInstantiation( (JavaTypeDescriptor<List>) javaTypeDescriptor );
		} else {
			dynamicInstantiation = SqmDynamicInstantiation.forClassInstantiation( javaTypeDescriptor );
		}

		for ( Selection<?> selection : selections ) {
			if ( selection instanceof SqmSelectClause ) {
				SqmSelectClause selectClause = (SqmSelectClause) selection;
				dynamicInstantiation.addArgument( (SqmSelectionBase) selectClause.getSelection() );
			} else {
				dynamicInstantiation.addArgument( (SqmSelectionBase) selection );
			}
		}

		return new SqmSelectClause(
				null,
				Collections.singletonList( dynamicInstantiation )
		);
	}

	@Override
	public CompoundSelection<Tuple> tuple(Selection<?>... selections) {
		return new SqmSelectClause(
				Tuple.class,
				validateSelections( selections )
		);
	}

	@Override
	public CompoundSelection<Object[]> array(Selection<?>... selections) {
		return new SqmSelectClause(
				Object[].class,
				validateSelections( selections )
		);
	}

	private List<SqmSelectionBase> validateSelections(Selection<?>[] selections) {
		return validateSelections( Arrays.asList( selections ) );
	}

	private List<SqmSelectionBase> validateSelections(List<Selection<?>> selections) {
		for ( int i = 0; i < selections.size() ; i++ ) {
			Selection<?> selection = selections.get( i );
			if ( selection instanceof SqmSelectClause ) {
				throw new IllegalArgumentException( "invalid array or tuple selection item at index " + i + " passed to array or tuple constructor" );
			}
			if ( selection instanceof CompoundSelection<?> ) {
				validateSelections( selection.getCompoundSelectionItems() );
			}
		}

		return (List<SqmSelectionBase>) (List) selections;
	}

	@Override
	public Order asc(Expression<?> x) {
		return new SqmSortSpecification(
				(SqmExpression) x,
				null,
				SqmSortOrder.ASCENDING
		);
	}

	@Override
	public Order desc(Expression<?> x) {
		return new SqmSortSpecification(
				(SqmExpression) x,
				null,
				SqmSortOrder.DESCENDING
		);
	}

	@Override
	public <N extends Number> Expression<Double> avg(Expression<N> x) {
		return new SqmAvgFunction(
				sessionFactory,
				(SqmExpression) x,
				doubleType
		);
	}

	@Override
	public <N extends Number> Expression<N> sum(Expression<N> x) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmSumFunction(
				sessionFactory,
				expression,
				sessionFactory.getTypeConfiguration()
						.resolveSumFunctionType( (BasicValuedExpressableType) expression.getExpressableType() )
		);
	}

	@Override
	public Expression<Long> sumAsLong(Expression<Integer> x) {
		return new SqmSumFunction(
				sessionFactory,
				(SqmExpression) x,
				longType
		);
	}

	@Override
	public Expression<Double> sumAsDouble(Expression<Float> x) {
		return new SqmSumFunction(
				sessionFactory,
				(SqmExpression) x,
				doubleType
		);
	}

	@Override
	public <N extends Number> Expression<N> max(Expression<N> x) {
		return new SqmMaxFunction( sessionFactory, (SqmExpression) x );
	}

	@Override
	public <N extends Number> Expression<N> min(Expression<N> x) {
		return new SqmMinFunction( sessionFactory, (SqmExpression) x );
	}

	@Override
	public <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> x) {
		return new SqmMaxFunction( sessionFactory, (SqmExpression) x );
	}

	@Override
	public <X extends Comparable<? super X>> Expression<X> least(Expression<X> x) {
		return new SqmMinFunction( sessionFactory, (SqmExpression) x );
	}

	@Override
	public Expression<Long> count(Expression<?> x) {
		return new SqmCountFunction( sessionFactory, (SqmExpression) x, longType );
	}

	@Override
	public Expression<Long> countDistinct(Expression<?> x) {
		return new SqmCountFunction( sessionFactory, (SqmExpression) x, longType, true );
	}

	@Override
	public Predicate exists(Subquery<?> subquery) {
		return null;
	}

	@Override
	public <Y> Expression<Y> all(Subquery<Y> subquery) {
		return null;
	}

	@Override
	public <Y> Expression<Y> some(Subquery<Y> subquery) {
		return null;
	}

	@Override
	public <Y> Expression<Y> any(Subquery<Y> subquery) {
		return null;
	}

	@Override
	public Predicate and(Expression<Boolean> x, Expression<Boolean> y) {
		return new AndSqmPredicate(
				sessionFactory,
				(SqmPredicate) wrap( x ),
				(SqmPredicate) wrap( y )
		);
	}

	@Override
	public Predicate and(Predicate... restrictions) {
		if ( restrictions == null || restrictions.length == 0 ) {
			return conjunction();
		}

		int index = restrictions.length - 1;
		SqmPredicate lastPredicate = (SqmPredicate) restrictions[index--];
		for ( ; index >= 0; index-- ) {
			lastPredicate = new AndSqmPredicate(
					sessionFactory,
					(SqmPredicate) restrictions[index],
					lastPredicate
			);
		}
		return lastPredicate;
	}

	@Override
	public Predicate or(Expression<Boolean> x, Expression<Boolean> y) {
		return new OrSqmPredicate(
				sessionFactory,
				(SqmPredicate) wrap( x ),
				(SqmPredicate) wrap( y )
		);
	}

	@Override
	public Predicate or(Predicate... restrictions) {
		if ( restrictions == null || restrictions.length == 0 ) {
			return disjunction();
		}

		int index = restrictions.length - 1;
		SqmPredicate lastPredicate = (SqmPredicate) restrictions[index--];
		for ( ; index >= 0; index-- ) {
			lastPredicate = new OrSqmPredicate(
					sessionFactory,
					(SqmPredicate) restrictions[index],
					lastPredicate
			);
		}
		return lastPredicate;
	}

	@Override
	public Predicate not(Expression<Boolean> restriction) {
		return new NegatedSqmPredicate( sessionFactory, (SqmPredicate) restriction );
	}

	@Override
	public Predicate conjunction() {
		return isTrue( new SqmLiteralTrue( sessionFactory, booleanType ) );
	}

	@Override
	public Predicate disjunction() {
		return isTrue( new SqmLiteralFalse( sessionFactory, booleanType ) );
	}

	@Override
	public Predicate isTrue(Expression<Boolean> x) {
		if ( x instanceof Predicate ) {
			return (Predicate) x;
		}
		return new BooleanExpressionSqmPredicate( sessionFactory, (SqmExpression) x );
	}

	@Override
	public Predicate isFalse(Expression<Boolean> x) {
		return not( isTrue( x ) );
	}

	@Override
	public Predicate isNull(Expression<?> x) {
		return new NullnessSqmPredicate( sessionFactory, (SqmExpression) x );
	}

	@Override
	public Predicate isNotNull(Expression<?> x) {
		return new NullnessSqmPredicate( sessionFactory, (SqmExpression) x, true );
	}

	@Override
	public Predicate equal(Expression<?> x, Expression<?> y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.EQUAL,
				(SqmExpression) x,
				(SqmExpression) y
		);
	}

	@Override
	public Predicate equal(Expression<?> x, Object y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.EQUAL,
				(SqmExpression) x,
				(SqmExpression) literal( y )
		);
	}

	@Override
	public Predicate notEqual(Expression<?> x, Expression<?> y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.NOT_EQUAL,
				(SqmExpression) x,
				(SqmExpression) y
		);
	}

	@Override
	public Predicate notEqual(Expression<?> x, Object y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.NOT_EQUAL,
				(SqmExpression) x,
				(SqmExpression) literal( y )
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Expression<? extends Y> y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.GREATER_THAN,
				(SqmExpression) x,
				(SqmExpression) y
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Y y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.GREATER_THAN,
				(SqmExpression) x,
				(SqmExpression) literal( y )
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Expression<? extends Y> y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.GREATER_THAN_OR_EQUAL,
				(SqmExpression) x,
				(SqmExpression) y
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> x, Y y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.GREATER_THAN_OR_EQUAL,
				(SqmExpression) x,
				(SqmExpression) literal( y )
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Expression<? extends Y> y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.LESS_THAN,
				(SqmExpression) x,
				(SqmExpression) y
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> x, Y y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.LESS_THAN,
				(SqmExpression) x,
				(SqmExpression) literal( y )
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Expression<? extends Y> y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.LESS_THAN_OR_EQUAL,
				(SqmExpression) x,
				(SqmExpression) y
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> x, Y y) {
		return new RelationalSqmPredicate(
				sessionFactory,
				RelationalPredicateOperator.LESS_THAN_OR_EQUAL,
				(SqmExpression) x,
				(SqmExpression) literal( y )
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Expression<? extends Y> x, Expression<? extends Y> y) {
		return new BetweenSqmPredicate(
				sessionFactory,
				(SqmExpression) v,
				(SqmExpression) x,
				(SqmExpression) y,
				false
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> v, Y x, Y y) {
		return new BetweenSqmPredicate(
				sessionFactory,
				(SqmExpression) v,
				(SqmExpression) literal( x ),
				(SqmExpression) literal( y ),
				false
		);
	}

	@Override
	public Predicate gt(Expression<? extends Number> x, Expression<? extends Number> y) {
		return greaterThan( (Expression<Comparable>) (Expression) x, (Expression<Comparable>) (Expression) y );
	}

	@Override
	public Predicate gt(Expression<? extends Number> x, Number y) {
		return greaterThan( (Expression<Comparable>) (Expression) x, (Comparable) y );
	}

	@Override
	public Predicate ge(Expression<? extends Number> x, Expression<? extends Number> y) {
		return greaterThanOrEqualTo( (Expression<Comparable>) (Expression) x, (Expression<Comparable>) (Expression) y );
	}

	@Override
	public Predicate ge(Expression<? extends Number> x, Number y) {
		return greaterThanOrEqualTo( (Expression<Comparable>) (Expression) x, (Comparable) y );
	}

	@Override
	public Predicate lt(Expression<? extends Number> x, Expression<? extends Number> y) {
		return lessThan( (Expression<Comparable>) (Expression) x, (Expression<Comparable>) (Expression) y );
	}

	@Override
	public Predicate lt(Expression<? extends Number> x, Number y) {
		return lessThan( (Expression<Comparable>) (Expression) x, (Comparable) y );
	}

	@Override
	public Predicate le(Expression<? extends Number> x, Expression<? extends Number> y) {
		return lessThanOrEqualTo( (Expression<Comparable>) (Expression) x, (Expression<Comparable>) (Expression) y );
	}

	@Override
	public Predicate le(Expression<? extends Number> x, Number y) {
		return lessThanOrEqualTo( (Expression<Comparable>) (Expression) x, (Comparable) y );
	}

	@Override
	public <N extends Number> Expression<N> neg(Expression<N> x) {
		return new SqmUnaryOperation(
				sessionFactory,
				SqmUnaryOperation.Operation.MINUS,
				(SqmExpression) x
		);
	}

	@Override
	public <N extends Number> Expression<N> abs(Expression<N> x) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmAbsFunction( sessionFactory, expression, (AllowableFunctionReturnType) expression.getExpressableType());
	}

	@Override
	public <N extends Number> Expression<N> sum(Expression<? extends N> x, Expression<? extends N> y) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.ADD,
				expression,
				(SqmExpression) y,
				expression.getExpressableType()
		);
	}

	@Override
	public <N extends Number> Expression<N> sum(Expression<? extends N> x, N y) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.ADD,
				expression,
				(SqmExpression) literal( y ),
				expression.getExpressableType()
		);
	}

	@Override
	public <N extends Number> Expression<N> sum(N x, Expression<? extends N> y) {
		SqmExpression expression = (SqmExpression) y;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.ADD,
				(SqmExpression) literal( x ),
				expression,
				expression.getExpressableType()
		);
	}

	@Override
	public <N extends Number> Expression<N> prod(Expression<? extends N> x, Expression<? extends N> y) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.MULTIPLY,
				expression,
				(SqmExpression) y,
				expression.getExpressableType()
		);
	}

	@Override
	public <N extends Number> Expression<N> prod(Expression<? extends N> x, N y) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.MULTIPLY,
				expression,
				(SqmExpression) literal( y ),
				expression.getExpressableType()
		);
	}

	@Override
	public <N extends Number> Expression<N> prod(N x, Expression<? extends N> y) {
		SqmExpression expression = (SqmExpression) y;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.MULTIPLY,
				(SqmExpression) literal( x ),
				expression,
				expression.getExpressableType()
		);
	}

	@Override
	public <N extends Number> Expression<N> diff(Expression<? extends N> x, Expression<? extends N> y) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.SUBTRACT,
				expression,
				(SqmExpression) y,
				expression.getExpressableType()
		);
	}

	@Override
	public <N extends Number> Expression<N> diff(Expression<? extends N> x, N y) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.SUBTRACT,
				expression,
				(SqmExpression) literal( y ),
				expression.getExpressableType()
		);
	}

	@Override
	public <N extends Number> Expression<N> diff(N x, Expression<? extends N> y) {
		SqmExpression expression = (SqmExpression) y;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.SUBTRACT,
				(SqmExpression) literal( x ),
				expression,
				expression.getExpressableType()
		);
	}

	@Override
	public Expression<Number> quot(Expression<? extends Number> x, Expression<? extends Number> y) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.QUOT,
				expression,
				(SqmExpression) y,
				expression.getExpressableType()
		);
	}

	@Override
	public Expression<Number> quot(Expression<? extends Number> x, Number y) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.QUOT,
				expression,
				(SqmExpression) literal( y ),
				expression.getExpressableType()
		);
	}

	@Override
	public Expression<Number> quot(Number x, Expression<? extends Number> y) {
		SqmExpression expression = (SqmExpression) y;
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.QUOT,
				(SqmExpression) literal( x ),
				expression,
				expression.getExpressableType()
		);
	}

	@Override
	public Expression<Integer> mod(Expression<Integer> x, Expression<Integer> y) {
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.QUOT,
				(SqmExpression) x,
				(SqmExpression) y,
				integerType
		);
	}

	@Override
	public Expression<Integer> mod(Expression<Integer> x, Integer y) {
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.MODULO,
				(SqmExpression) x,
				(SqmExpression) literal( y ),
				integerType
		);
	}

	@Override
	public Expression<Integer> mod(Integer x, Expression<Integer> y) {
		return new SqmBinaryArithmetic(
				sessionFactory,
				SqmBinaryArithmetic.Operation.MODULO,
				(SqmExpression) literal( x ),
				(SqmExpression) y,
				integerType
		);
	}

	@Override
	public Expression<Double> sqrt(Expression<? extends Number> x) {
		return new SqmSqrtFunction(
				sessionFactory,
				(SqmExpression) x,
				doubleType
		);
	}

	@Override
	public Expression<Long> toLong(Expression<? extends Number> number) {
		return (Expression<Long>) number;
	}

	@Override
	public Expression<Integer> toInteger(Expression<? extends Number> number) {
		return (Expression<Integer>) number;
	}

	@Override
	public Expression<Float> toFloat(Expression<? extends Number> number) {
		return (Expression<Float>) number;
	}

	@Override
	public Expression<Double> toDouble(Expression<? extends Number> number) {
		return (Expression<Double>) number;
	}

	@Override
	public Expression<BigDecimal> toBigDecimal(Expression<? extends Number> number) {
		return (Expression<BigDecimal>) number;
	}

	@Override
	public Expression<BigInteger> toBigInteger(Expression<? extends Number> number) {
		return (Expression<BigInteger>) number;
	}

	@Override
	public Expression<String> toString(Expression<Character> character) {
		return (Expression<String>) (Expression) character;
	}

	@Override
	public <T> Expression<T> literal(T value) {
		if ( value == null ) {
			throw new NullPointerException( "Value passed as `constant value` cannot be null" );
		}

		Expression<T> expression = null;
		final Class<T> javaType = (Class<T>) value.getClass();
		if ( javaType == Class.class ) {
			EntityDescriptor<?> entityDescriptor = sessionFactory.getMetamodel().findEntityDescriptor( (Class<?>) value );
			if ( entityDescriptor != null ) {
				return new SqmLiteralEntityType( sessionFactory, entityDescriptor );
			}
		}

		BasicType<T> basicType = sessionFactory.getTypeConfiguration().getBasicTypeRegistry().getBasicType( javaType );
		if ( basicType != null ) {
			 expression = basicType.getJavaTypeDescriptor().createLiteralExpression(sessionFactory, basicType, value );
		}
		if ( expression != null ) {
			return expression;
		}

		throw new QueryException(
				"Unexpected literal expression [value=" + value +
						", javaType=" + javaType.getName() +
						"]; expecting boolean, int, long, float, double, BigInteger, BigDecimal, char, or String"
		);
	}

	@Override
	public <T> Expression<T> nullLiteral(Class<T> resultClass) {
		return new SqmLiteralNull( sessionFactory, resultClass );
	}

	@Override
	public <T> ParameterExpression<T> parameter(Class<T> paramClass) {
		return new SqmAnonymousParameter(
				sessionFactory,
				true,
				paramClass
		);
	}

	@Override
	public <T> ParameterExpression<T> parameter(Class<T> paramClass, String name) {
		return new SqmNamedParameter(
				sessionFactory,
				name,
				true,
				paramClass
		);
	}

	@Override
	public <C extends Collection<?>> Predicate isEmpty(Expression<C> collection) {
		return new EmptinessSqmPredicate( sessionFactory, (SqmPluralAttributeReference) collection);
	}

	@Override
	public <C extends Collection<?>> Predicate isNotEmpty(Expression<C> collection) {
		return new EmptinessSqmPredicate( sessionFactory, (SqmPluralAttributeReference) collection, true);
	}

	@Override
	public <C extends Collection<?>> Expression<Integer> size(Expression<C> collection) {
		return new SqmCollectionSize( sessionFactory, (SqmPluralAttributeReference) collection, integerType );
	}

	@Override
	public <C extends Collection<?>> Expression<Integer> size(C collection) {
		return literal( collection == null ? 0 : collection.size() );
	}

	@Override
	public <M extends Map<?, ?>> Predicate isMapEmpty(Expression<M> mapExpression) {
		return new EmptinessSqmPredicate( sessionFactory, (SqmPluralAttributeReference) mapExpression);
	}

	@Override
	public <M extends Map<?, ?>> Predicate isMapNotEmpty(Expression<M> mapExpression) {
		return new EmptinessSqmPredicate( sessionFactory, (SqmPluralAttributeReference) mapExpression, true);
	}

	@Override
	public <M extends Map<?, ?>> Expression<Integer> mapSize(Expression<M> mapExpression) {
		return new SqmCollectionSize( sessionFactory, (SqmPluralAttributeReference) mapExpression, integerType );
	}

	@Override
	public <M extends Map<?, ?>> Expression<Integer> mapSize(M map) {
		return literal( map == null ? 0 : map.size() );
	}

	@Override
	public <E, C extends Collection<E>> Predicate isMember(Expression<E> elem, Expression<C> collection) {
		return new MemberOfSqmPredicate( sessionFactory, (SqmExpression) elem, (SqmPluralAttributeReference) collection );
	}

	@Override
	public <E, C extends Collection<E>> Predicate isMember(E elem, Expression<C> collection) {
		return new MemberOfSqmPredicate( sessionFactory, (SqmExpression) literal( elem ), (SqmPluralAttributeReference) collection );
	}

	@Override
	public <E, C extends Collection<E>> Predicate isNotMember(Expression<E> elem, Expression<C> collection) {
		return new MemberOfSqmPredicate( sessionFactory, (SqmExpression) elem, (SqmPluralAttributeReference) collection, true );
	}

	@Override
	public <E, C extends Collection<E>> Predicate isNotMember(E elem, Expression<C> collection) {
		return new MemberOfSqmPredicate( sessionFactory, (SqmExpression) literal( elem ), (SqmPluralAttributeReference) collection, true );
	}

	@Override
	public <V, M extends Map<?, V>> Expression<Collection<V>> values(M map) {
		return literal( map.values() );
	}

	@Override
	public <K, M extends Map<K, ?>> Expression<Set<K>> keys(M map) {
		return literal( map.keySet() );
	}

	@Override
	public Predicate like(Expression<String> x, Expression<String> pattern) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) pattern
		);
	}

	@Override
	public Predicate like(Expression<String> x, String pattern) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) literal( pattern )
		);
	}

	@Override
	public Predicate like(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) pattern,
				(SqmExpression) escapeChar
		);
	}

	@Override
	public Predicate like(Expression<String> x, Expression<String> pattern, char escapeChar) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) pattern,
				(SqmExpression) literal( escapeChar )
		);
	}

	@Override
	public Predicate like(Expression<String> x, String pattern, Expression<Character> escapeChar) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) literal( pattern ),
				(SqmExpression) escapeChar
		);
	}

	@Override
	public Predicate like(Expression<String> x, String pattern, char escapeChar) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) literal( pattern ),
				(SqmExpression) literal( escapeChar )
		);
	}

	@Override
	public Predicate notLike(Expression<String> x, Expression<String> pattern) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) pattern,
				null,
				true
		);
	}

	@Override
	public Predicate notLike(Expression<String> x, String pattern) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) literal( pattern ),
				null,
				true
		);
	}

	@Override
	public Predicate notLike(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) pattern,
				(SqmExpression) escapeChar,
				true
		);
	}

	@Override
	public Predicate notLike(Expression<String> x, Expression<String> pattern, char escapeChar) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) pattern,
				(SqmExpression) literal( escapeChar ),
				true
		);
	}

	@Override
	public Predicate notLike(Expression<String> x, String pattern, Expression<Character> escapeChar) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) literal( pattern ),
				(SqmExpression) escapeChar,
				true
		);
	}

	@Override
	public Predicate notLike(Expression<String> x, String pattern, char escapeChar) {
		return new LikeSqmPredicate(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) literal( pattern ),
				(SqmExpression) literal( escapeChar ),
				true
		);
	}

	@Override
	public Expression<String> concat(Expression<String> x, Expression<String> y) {
		return new SqmConcat(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) y
		);
	}

	@Override
	public Expression<String> concat(Expression<String> x, String y) {
		return new SqmConcat(
				sessionFactory,
				(SqmExpression) x,
				(SqmExpression) literal( y )
		);
	}

	@Override
	public Expression<String> concat(String x, Expression<String> y) {
		return new SqmConcat(
				sessionFactory,
				(SqmExpression) literal( x ),
				(SqmExpression) y
		);
	}

	@Override
	public Expression<String> substring(Expression<String> x, Expression<Integer> from) {
		return new SqmSubstringFunction(
				sessionFactory,
				stringType,
				(SqmExpression) x,
				(SqmExpression) from,
				null
		);
	}

	@Override
	public Expression<String> substring(Expression<String> x, int from) {
		return new SqmSubstringFunction(
				sessionFactory,
				stringType,
				(SqmExpression) x,
				(SqmExpression) literal( from ),
				null
		);
	}

	@Override
	public Expression<String> substring(Expression<String> x, Expression<Integer> from, Expression<Integer> len) {
		return new SqmSubstringFunction(
				sessionFactory,
				stringType,
				(SqmExpression) x,
				(SqmExpression) from,
				(SqmExpression) len
		);
	}

	@Override
	public Expression<String> substring(Expression<String> x, int from, int len) {
		return new SqmSubstringFunction(
				sessionFactory,
				stringType,
				(SqmExpression) x,
				(SqmExpression) literal( from ),
				(SqmExpression) literal( len )
		);
	}

	@Override
	public Expression<String> trim(Expression<String> x) {
		return trim( Trimspec.BOTH, literal( ' ' ), x );
	}

	@Override
	public Expression<String> trim(Trimspec ts, Expression<String> x) {
		return trim( ts, literal( ' ' ), x );
	}

	@Override
	public Expression<String> trim(Expression<Character> t, Expression<String> x) {
		return trim( Trimspec.BOTH, t, x );
	}

	@Override
	public Expression<String> trim(Trimspec ts, Expression<Character> t, Expression<String> x) {
		return new SqmTrimFunction(
				sessionFactory,
				stringType,
				getTrimSpecification( ts ),
				(SqmExpression) literal( ' ' ),
				(SqmExpression) x
		);
	}

	@Override
	public Expression<String> trim(char t, Expression<String> x) {
		return trim( Trimspec.BOTH, literal( t ), x );
	}

	@Override
	public Expression<String> trim(Trimspec ts, char t, Expression<String> x) {
		return trim( ts, literal( t ), x );
	}

	private static TrimSpecification getTrimSpecification(Trimspec ts) {
		switch (ts) {
			case BOTH: return TrimSpecification.BOTH;
			case LEADING: return TrimSpecification.LEADING;
			case TRAILING: return TrimSpecification.TRAILING;
		}

		throw new IllegalArgumentException( "unexpected trimspec: " + ts );
	}

	@Override
	public Expression<String> lower(Expression<String> x) {
		return new SqmLowerFunction(
				sessionFactory,
				stringType,
				(SqmExpression) x
		);
	}

	@Override
	public Expression<String> upper(Expression<String> x) {
		return new SqmUpperFunction(
				sessionFactory,
				stringType,
				(SqmExpression) x
		);
	}

	@Override
	public Expression<Integer> length(Expression<String> x) {
		return new SqmLengthFunction(
				sessionFactory,
				(SqmExpression) x,
				integerType
		);
	}

	@Override
	public Expression<Integer> locate(Expression<String> x, Expression<String> pattern) {
		return new SqmLocateFunction(
				sessionFactory,
				(SqmExpression) pattern,
				(SqmExpression) x,
				null,
				integerType
		);
	}

	@Override
	public Expression<Integer> locate(Expression<String> x, String pattern) {
		return new SqmLocateFunction(
				sessionFactory,
				(SqmExpression) literal( pattern ),
				(SqmExpression) x,
				null,
				integerType
		);
	}

	@Override
	public Expression<Integer> locate(Expression<String> x, Expression<String> pattern, Expression<Integer> from) {
		return new SqmLocateFunction(
				sessionFactory,
				(SqmExpression) pattern,
				(SqmExpression) x,
				(SqmExpression) from,
				integerType
		);
	}

	@Override
	public Expression<Integer> locate(Expression<String> x, String pattern, int from) {
		return new SqmLocateFunction(
				sessionFactory,
				(SqmExpression) literal( pattern ),
				(SqmExpression) x,
				(SqmExpression) literal( from ),
				integerType
		);
	}

	@Override
	public Expression<Date> currentDate() {
		return new SqmCurrentDateFunction( sessionFactory );
	}

	@Override
	public Expression<Timestamp> currentTimestamp() {
		return new SqmCurrentTimestampFunction( sessionFactory );
	}

	@Override
	public Expression<Time> currentTime() {
		return new SqmCurrentTimeFunction( sessionFactory );
	}

	@Override
	public <T> In<T> in(Expression<? extends T> expression) {
		return (In) new InListSqmPredicate( sessionFactory, (SqmExpression) expression );
	}

	@Override
	public Predicate in(Expression testExpression, Object... values) {
		List<SqmExpression> expressions = new ArrayList<>(values.length);
		for (Object value : values) {
			expressions.add((SqmExpression) literal(value));
		}
		return new InListSqmPredicate( sessionFactory, (SqmExpression) testExpression, expressions, false );
	}

	@Override
	public Predicate in(Expression testExpression, Expression[] values) {
		List<SqmExpression> expressions = new ArrayList<>(values.length);
		for (Object value : values) {
			expressions.add((SqmExpression) value);
		}
		return new InListSqmPredicate( sessionFactory, (SqmExpression) testExpression, expressions, false );
	}

	@Override
	public Predicate in(Expression testExpression, Collection values) {
		List<SqmExpression> expressions = new ArrayList<>(values.size());
		for (Object value : values) {
			expressions.add((SqmExpression) literal(value));
		}
		return new InListSqmPredicate( sessionFactory, (SqmExpression) testExpression, expressions, false );
	}

	@Override
	public <Y> Expression<Y> coalesce(Expression<? extends Y> x, Expression<? extends Y> y) {
		SqmExpression expression = (SqmExpression) x;
		List<SqmExpression> arguments = new ArrayList<>( 2 );
		arguments.add( expression );
		arguments.add( (SqmExpression) y);
		return new SqmCoalesceFunction(
				sessionFactory,
				(AllowableFunctionReturnType) expression.getExpressableType(),
				arguments
		);
	}

	@Override
	public <Y> Expression<Y> coalesce(Expression<? extends Y> x, Y y) {
		SqmExpression expression = (SqmExpression) x;
		List<SqmExpression> arguments = new ArrayList<>( 2 );
		arguments.add( expression );
		arguments.add( (SqmExpression) literal( y ) );
		return new SqmCoalesceFunction(
				sessionFactory,
				(AllowableFunctionReturnType) expression.getExpressableType(),
				arguments
		);
	}

	@Override
	public <Y> Expression<Y> nullif(Expression<Y> x, Expression<?> y) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmNullifFunction(
				sessionFactory,
				expression,
				(SqmExpression) y,
				(AllowableFunctionReturnType) expression.getExpressableType()
		);
	}

	@Override
	public <Y> Expression<Y> nullif(Expression<Y> x, Y y) {
		SqmExpression expression = (SqmExpression) x;
		return new SqmNullifFunction(
				sessionFactory,
				expression,
				(SqmExpression) literal( y ),
				(AllowableFunctionReturnType) expression.getExpressableType()
		);
	}

	@Override
	public <T> Coalesce<T> coalesce() {
		return new SqmCoalesceFunction( sessionFactory );
	}

	@Override
	public <C, R> SimpleCase<C, R> selectCase(Expression<? extends C> expression) {
		return new SqmCaseSimple( sessionFactory, (SqmExpression) expression );
	}

	@Override
	public <R> Case<R> selectCase() {
		return new SqmCaseSearched( sessionFactory );
	}

	@Override
	public <T> Expression<T> function(String name, Class<T> type, Expression<?>... args) {
		return new SqmGenericFunction(
				sessionFactory,
				name,
				sessionFactory.getTypeConfiguration()
					.getBasicTypeRegistry()
					.getBasicType( type ),
				(List<SqmExpression>) (List) Arrays.asList( args )
		);
	}

	@Override
	public <X, T, V extends T> Join<X, V> treat(Join<X, T> join, Class<V> type) {
		return null;
	}

	@Override
	public <X, T, E extends T> CollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type) {
		return null;
	}

	@Override
	public <X, T, E extends T> SetJoin<X, E> treat(SetJoin<X, T> join, Class<E> type) {
		return null;
	}

	@Override
	public <X, T, E extends T> ListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type) {
		return null;
	}

	@Override
	public <X, K, T, V extends T> MapJoin<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type) {
		return null;
	}

	@Override
	public <X, T extends X> Path<T> treat(Path<X> path, Class<T> type) {
		return null;
	}

	@Override
	public <X, T extends X> Root<T> treat(Root<X> root, Class<T> type) {
		return null;
	}
}
