/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.internal.CriteriaQueryCreationContext;
import org.hibernate.query.sqm.produce.internal.SqmCriteriaCopyContext;
import org.hibernate.query.sqm.produce.internal.SqmFromBuilderFromClauseStandard;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.expression.*;
import org.hibernate.query.sqm.tree.SqmStatement;
import org.hibernate.query.sqm.SemanticException;
import org.hibernate.query.sqm.tree.from.SqmFromClause;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.group.SqmGroupByClause;
import org.hibernate.query.sqm.tree.order.SqmOrderByClause;
import org.hibernate.query.sqm.tree.paging.SqmLimitOffsetClause;
import org.hibernate.query.sqm.tree.predicate.SqmHavingClause;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;
import org.hibernate.query.sqm.tree.select.SqmSelectClause;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.type.spi.TypeConfiguration;

import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.Subquery;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmStatement implements SqmStatement, ParameterCollector, CommonAbstractCriteria {
	private final SqmCreationContext creationContext;
	private Map<String,SqmNamedParameter> namedQueryParameters;
	private Map<Integer,SqmPositionalParameter> positionalQueryParameters;
	private Map<SqmAnonymousParameter,SqmAnonymousParameter> anonymousQueryParameters;

	public AbstractSqmStatement(SqmCreationContext creationContext) {
		this.creationContext = creationContext;
		creationContext.setCurrentContainingQuery( this );
	}

	protected SessionFactoryImplementor getSessionFactory() {
		return creationContext.getSessionFactory();
	}

	protected HibernateCriteriaBuilder criteriaBuilder() {
		return creationContext.getSessionFactory().getCriteriaBuilder();
	}

	protected final SqmRoot createRoot(SqmFromClause fromClause, Class<?> entityClass) {
		EntityValuedNavigable<?> entityReference = (EntityValuedNavigable<?>) creationContext.getSessionFactory()
				.getMetamodel()
				.resolveEntityReference( entityClass );
		SqmFromElementSpace oldSpace = creationContext.getCurrentFromElementSpace();

		try {
			SqmFromElementSpace fromElementSpace = fromClause == null ? null : fromClause.makeFromElementSpace();
			creationContext.getCurrentSqmFromElementSpaceCoordAccess().setCurrentSqmFromElementSpace( fromElementSpace );
			SqmFromBuilderFromClauseStandard fromBuilder = new SqmFromBuilderFromClauseStandard(
					creationContext.getImplicitAliasGenerator().generateUniqueImplicitAlias(),
					creationContext
			);
			return fromBuilder.buildRoot( entityReference );
		} finally {
			creationContext.getCurrentSqmFromElementSpaceCoordAccess().setCurrentSqmFromElementSpace( oldSpace );
		}
	}

	@Override
	public SqmStatement copy() {
		SqmCreationContext creationContext = CriteriaQueryCreationContext.forDml( getSessionFactory() );
		SqmStatement statement = copy( new SqmCriteriaCopyContext( creationContext ) );
		statement.wrapUp();
		return statement;
	}

	@Override
	public void addParameter(SqmNamedParameter parameter) {
		assert parameter.getName() != null;
		assert parameter.getPosition() == null;

		if ( namedQueryParameters == null ) {
			namedQueryParameters = new HashMap<>();
		}

		namedQueryParameters.put( parameter.getName(), parameter );
	}

	@Override
	public void addParameter(SqmPositionalParameter parameter) {
		assert parameter.getPosition() != null;
		assert parameter.getName() == null;

		if ( positionalQueryParameters == null ) {
			positionalQueryParameters = new HashMap<>();
		}

		positionalQueryParameters.put( parameter.getPosition(), parameter );
	}

	@Override
	public void addParameter(SqmAnonymousParameter parameter) {
		assert parameter.getPosition() == null;
		assert parameter.getName() == null;

		if ( anonymousQueryParameters == null ) {
			anonymousQueryParameters = new HashMap<>();
		}

		anonymousQueryParameters.put( parameter, parameter );
	}

	@Override
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
		if ( anonymousQueryParameters != null ) {
			parameters.addAll( anonymousQueryParameters.values() );
		}
		return parameters;
	}

	@Override
	public <U> Subquery<U> subquery(Class<U> type) {
		SessionFactoryImplementor sessionFactory = getSessionFactory();
		ExpressableType expressableType = sessionFactory.getMetamodel().findEntityDescriptor( type );
		if ( expressableType == null ) {
			expressableType = sessionFactory.getTypeConfiguration().getBasicTypeRegistry().getBasicType( type );
		}

		return new SqmSubQuery(
				creationContext,
				this,
				new SqmQuerySpec(
						new SqmFromClause(),
						new SqmSelectClause(false, type),
						new SqmWhereClause(),
						new SqmGroupByClause(),
						new SqmHavingClause(),
						new SqmOrderByClause(),
						new SqmLimitOffsetClause()
				),
				expressableType
		);
	}
}
