/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.internal;

import org.hibernate.query.criteria.internal.CriteriaQueryCreationContext;
import org.hibernate.query.sqm.produce.internal.SqmCriteriaCopyContext;
import org.hibernate.query.sqm.produce.internal.SqmParameterCollector;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.SqmStatement;
import org.hibernate.query.sqm.tree.expression.SqmAnonymousParameter;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.SqmNamedParameter;
import org.hibernate.query.sqm.tree.expression.SqmPositionalParameter;
import org.hibernate.query.sqm.tree.group.SqmGroupSpecification;
import org.hibernate.query.sqm.tree.order.SqmSortSpecification;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.query.sqm.tree.select.SqmSelectClause;
import org.hibernate.query.sqm.tree.select.SqmSelectionBase;

import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Steve Ebersole
 */
public class SqmSelectStatementImpl<T> extends AbstractSqmStatement implements SqmSelectStatement, CriteriaQuery<T> {
	private final Class<T> resultType;
	private SqmQuerySpec querySpec;

	public SqmSelectStatementImpl(SqmCreationContext creationContext, Class<T> resultType) {
		this( creationContext, resultType, null );
	}

	public SqmSelectStatementImpl(SqmCreationContext creationContext, Class<T> resultType, SqmQuerySpec querySpec) {
		super( creationContext );
		this.resultType = resultType;
		this.querySpec = querySpec;
	}

	@Override
	public SqmSelectStatement copy() {
		SqmCreationContext creationContext = CriteriaQueryCreationContext.forQuery( getSessionFactory() );
		SqmSelectStatement statement = copy( new SqmCriteriaCopyContext( creationContext ) );
		statement.wrapUp();
		return statement;
	}

	@Override
	public SqmSelectStatement copy(SqmCopyContext context) {
		// First register the copy instance so that subqueries can look it up
		SqmSelectStatementImpl statement = new SqmSelectStatementImpl(
				context.getCreationContext(),
				resultType,
				null
		);
		// only then copy the query spec
		statement.querySpec = querySpec.copy( context );
		return statement;
	}

	@Override
	public SqmQuerySpec getQuerySpec() {
		return querySpec;
	}

	public void applyQuerySpec(SqmQuerySpec querySpec) {
		if ( this.querySpec != null ) {
			throw new IllegalStateException( "SqmQuerySpec was already defined for select-statement" );
		}
		this.querySpec = querySpec;
	}

	@Override
	public CriteriaQuery<T> select(Selection<? extends T> selection) {
		if ( !resultType.isAssignableFrom( selection.getJavaType() ) ) {
			throw new IllegalArgumentException( "expected selection java type [" + resultType.getName() + "] but was [" + selection.getJavaType().getName() + "]" );
		}
		if ( selection instanceof SqmSelectClause ) {
			querySpec.getSelectClause().setSelection( (SqmSelectClause) selection );
		} else {
			querySpec.getSelectClause().setSelection( (SqmSelectionBase) selection );
		}
		return this;
	}

	@Override
	public CriteriaQuery<T> multiselect(Selection[] selections) {
		if ( resultType == Tuple.class ) {
			querySpec.getSelectClause().setSelection( (SqmSelectClause) criteriaBuilder().tuple( selections ) );
		} else if ( resultType.isArray() ) {
			querySpec.getSelectClause().setSelection( (SqmSelectClause) criteriaBuilder().array( selections ) );
		} else if ( resultType == Object.class ) {
			if ( selections.length > 1 ) {
				querySpec.getSelectClause().setSelection( (SqmSelectClause) criteriaBuilder().array( selections ) );
			} else {
				querySpec.getSelectClause().setSelection( (SqmSelectionBase) selections[ 0 ] );
			}
		} else {
			querySpec.getSelectClause().setSelection( (SqmSelectClause) criteriaBuilder().construct( resultType, selections ) );
		}

		return this;
	}

	@Override
	public CriteriaQuery<T> multiselect(List<Selection<?>> list) {
		return multiselect( list.toArray( new Selection[ list.size() ] ) );
	}

	@Override
	public <X> Root<X> from(Class<X> entityClass) {
		return createRoot( querySpec.getFromClause(), entityClass );
	}

	@Override
	public <X> Root<X> from(EntityType<X> entity) {
		return from( entity.getJavaType() );
	}

	@Override
	public CriteriaQuery<T> where(Expression restriction) {
		querySpec.getWhereClause().setPredicate( (SqmPredicate) criteriaBuilder().wrap( restriction ) );
		return this;
	}

	@Override
	public CriteriaQuery<T> where(Predicate... restrictions) {
		querySpec.getWhereClause().setPredicate( (SqmPredicate) criteriaBuilder().and( restrictions ) );
		return this;
	}

	@Override
	public CriteriaQuery<T> groupBy(Expression[] grouping) {
		return groupBy( Arrays.asList( grouping ) );
	}

	@Override
	public CriteriaQuery<T> groupBy(List grouping) {
		List<SqmGroupSpecification> groupSpecifications = new ArrayList<>( grouping.size() );
		for (Object o : grouping) {
			groupSpecifications.add( new SqmGroupSpecification( (SqmExpression) o, null ) );
		}
		querySpec.getGroupByClause().setGroupBySpecifications( groupSpecifications );
		return this;
	}

	@Override
	public CriteriaQuery<T> having(Expression restriction) {
		querySpec.getHavingClause().setPredicate( (SqmPredicate) criteriaBuilder().wrap( restriction ) );
		return this;
	}

	@Override
	public CriteriaQuery<T> having(Predicate... restrictions) {
		querySpec.getHavingClause().setPredicate( (SqmPredicate) criteriaBuilder().and( restrictions ) );
		return this;
	}

	@Override
	public CriteriaQuery<T> orderBy(Order... orders) {
		querySpec.getOrderByClause().setSortSpecifications( (List<SqmSortSpecification>) (List) Arrays.asList( orders ) );
		return this;
	}

	@Override
	public CriteriaQuery<T> orderBy(List orders) {
		querySpec.getOrderByClause().setSortSpecifications( (List<SqmSortSpecification>) orders );
		return this;
	}

	@Override
	public CriteriaQuery<T> distinct(boolean distinct) {
		querySpec.getSelectClause().setDistinct( distinct );
		return this;
	}

	@Override
	public List<Order> getOrderList() {
		return (List<Order>) (List) querySpec.getOrderByClause().getSortSpecifications();
	}

	@Override
	public void wrapUp() {
		Set<ParameterExpression<?>> parameters = getParameters();
		for ( ParameterExpression<?> parameter : parameters ) {
			if ( parameter instanceof SqmNamedParameter ) {
				addParameter( (SqmNamedParameter) parameter );
			} else if ( parameter instanceof SqmPositionalParameter) {
				addParameter( (SqmPositionalParameter) parameter );
			} else {
				addParameter( (SqmAnonymousParameter) parameter );
			}
		}

		super.wrapUp();
	}

	@Override
	public Set<ParameterExpression<?>> getParameters() {
		return SqmParameterCollector.getParameters( this, getSessionFactory() );
	}

	@Override
	public Set<Root<?>> getRoots() {
		return querySpec.getFromClause()
				.getFromElementSpaces()
				.stream()
				.map( space -> (Root<?>) space.getRoot() )
				.collect(Collectors.toSet());
	}

	@Override
	public Selection<T> getSelection() {
		return (Selection<T>) querySpec.getSelectClause().getSelection();
	}

	@Override
	public List<Expression<?>> getGroupList() {
		return querySpec.getGroupByClause()
				.getGroupBySpecifications()
				.stream()
				.map( groupSpecification -> (Expression<?>) groupSpecification.getGroupExpression() )
				.collect(Collectors.toList());
	}

	@Override
	public Predicate getGroupRestriction() {
		return querySpec.getHavingClause().getPredicate();
	}

	@Override
	public boolean isDistinct() {
		return querySpec.getSelectClause().isDistinct();
	}

	@Override
	public Class<T> getResultType() {
		return resultType;
	}

	@Override
	public Predicate getRestriction() {
		return querySpec.getWhereClause().getPredicate();
	}
}
