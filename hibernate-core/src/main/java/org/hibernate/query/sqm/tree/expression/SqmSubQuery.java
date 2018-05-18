/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.HibernateException;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.produce.internal.SqmFromBuilderFromClauseStandard;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.from.SqmFromClause;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.group.SqmGroupSpecification;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.query.sqm.tree.select.SqmDynamicInstantiation;
import org.hibernate.query.sqm.tree.select.SqmSelectClause;
import org.hibernate.query.sqm.tree.select.SqmSelectableNode;
import org.hibernate.query.sqm.tree.select.SqmSelectionBase;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Steve Ebersole
 */
public class SqmSubQuery extends AbstractSqmExpression implements SqmExpression, Subquery {
	private final SqmCreationContext creationContext;
	private final CommonAbstractCriteria containingQuery;
	private final SqmQuerySpec querySpec;
	private final ExpressableType expressableType;

	public SqmSubQuery(SqmCreationContext creationContext, Supplier<SqmQuerySpec> querySpecSupplier) {
		super( creationContext.getSessionFactory() );
		this.creationContext = creationContext;
		this.containingQuery = creationContext.getCurrentContainingQuery();
		try {
			creationContext.setCurrentContainingQuery( this );
			this.querySpec = querySpecSupplier.get();
			this.expressableType = determineTypeDescriptor( querySpec.getSelectClause() );
		} finally {
			creationContext.setCurrentContainingQuery( containingQuery );
		}
	}

	private SqmSubQuery(SqmCreationContext creationContext, Supplier<SqmQuerySpec> querySpecSupplier, ExpressableType expressableType) {
		super( creationContext.getSessionFactory() );
		this.creationContext = creationContext;
		this.containingQuery = creationContext.getCurrentContainingQuery();
		try {
			creationContext.setCurrentContainingQuery( this );
			this.querySpec = querySpecSupplier.get();
			this.expressableType = expressableType;
		} finally {
			creationContext.setCurrentContainingQuery( containingQuery );
		}
	}

	public SqmSubQuery(SqmCreationContext creationContext, CommonAbstractCriteria containingQuery, SqmQuerySpec querySpec, ExpressableType expressableType) {
		super( creationContext.getSessionFactory() );
		this.creationContext = creationContext;
		this.containingQuery = containingQuery;
		this.querySpec = querySpec;
		this.expressableType = expressableType;
	}

	private static ExpressableType determineTypeDescriptor(SqmSelectClause selectClause) {
		if ( selectClause.getSelections().size() != 1 ) {
			return null;
		}

		final SqmSelectableNode selectableNode = selectClause.getSelections().get( 0 ).getSelectableNode();
		if ( SqmDynamicInstantiation.class.isInstance( selectableNode ) ) {
			throw new HibernateException( "Illegal use of dynamic-instantiation in sub-query" );
		}

		return ( (SqmExpression) selectableNode ).getExpressableType();
	}

	@Override
	public SqmSubQuery copy(SqmCopyContext context) {
		return new SqmSubQuery(
                context.getCreationContext(),
				() -> querySpec.copy( context ),
				expressableType
		);
	}

	private HibernateCriteriaBuilder criteriaBuilder() {
		return creationContext.getSessionFactory().getCriteriaBuilder();
	}

	@Override
	public ExpressableType getExpressableType() {
		return expressableType;
	}

	@Override
	public ExpressableType getInferableType() {
		return getExpressableType();
	}

	public SqmQuerySpec getQuerySpec() {
		return querySpec;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSubQueryExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "<subquery>";
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return expressableType.getJavaTypeDescriptor();
	}

//	@Override
//	public QueryResult createQueryResult(
//			Expression expression, String resultVariable, QueryResultCreationContext creationContext) {
//		throw new TreeException( "Selecting a SubQuery type is not allowed.");
//	}

	private SqmRoot createRoot(SqmFromClause fromClause, Class<?> entityClass) {
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
	public Root from(Class entityClass) {
		return createRoot( querySpec.getFromClause(), entityClass );
	}

	@Override
	public Root from(EntityType entity) {
		return from( entity.getJavaType() );
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
	public Set<Join<?, ?>> getCorrelatedJoins() {
		return null;
	}

	@Override
	public Root correlate(Root parentRoot) {
		return null;
	}

	@Override
	public Join correlate(Join parentJoin) {
		return null;
	}

	@Override
	public CollectionJoin correlate(CollectionJoin parentCollection) {
		return null;
	}

	@Override
	public SetJoin correlate(SetJoin parentSet) {
		return null;
	}

	@Override
	public ListJoin correlate(ListJoin parentList) {
		return null;
	}

	@Override
	public MapJoin correlate(MapJoin parentMap) {
		return null;
	}

	@Override
	public AbstractQuery<?> getParent() {
		if ( containingQuery instanceof AbstractQuery<?> ) {
			return (AbstractQuery<?>) containingQuery;

		}
		return null;
	}

	@Override
	public CommonAbstractCriteria getContainingQuery() {
		return containingQuery;
	}

	@Override
	public Subquery select(Expression expression) {
		if ( !getResultType().isAssignableFrom( expression.getJavaType() ) ) {
			throw new IllegalArgumentException( "expected selection java type [" + getResultType().getName() + "] but was [" + expression.getJavaType().getName() + "]" );
		}
		if ( expression instanceof SqmSelectClause) {
			querySpec.getSelectClause().setSelection( (SqmSelectClause) expression );
		} else {
			querySpec.getSelectClause().setSelection( (SqmSelectionBase) expression );
		}
		return this;
	}

	@Override
	public Subquery where(Expression restriction) {
		querySpec.getWhereClause().setPredicate( (SqmPredicate) criteriaBuilder().wrap( restriction ) );
		return this;
	}

	@Override
	public Subquery where(Predicate... restrictions) {
		querySpec.getWhereClause().setPredicate( (SqmPredicate) criteriaBuilder().and( restrictions ) );
		return this;
	}

	@Override
	public Subquery groupBy(Expression[] grouping) {
		return groupBy( Arrays.asList( grouping ) );
	}

	@Override
	public Subquery groupBy(List grouping) {
		List<SqmGroupSpecification> groupSpecifications = new ArrayList<>( grouping.size() );
		for (Object o : grouping) {
			groupSpecifications.add( new SqmGroupSpecification( (SqmExpression) o, null ) );
		}
		querySpec.getGroupByClause().setGroupBySpecifications( groupSpecifications );
		return this;
	}

	@Override
	public Subquery having(Expression restriction) {
		querySpec.getHavingClause().setPredicate( (SqmPredicate) criteriaBuilder().wrap( restriction ) );
		return this;
	}

	@Override
	public Subquery having(Predicate... restrictions) {
		querySpec.getHavingClause().setPredicate( (SqmPredicate) criteriaBuilder().and( restrictions ) );
		return this;
	}

	@Override
	public Subquery distinct(boolean distinct) {
		querySpec.getSelectClause().setDistinct( distinct );
		return this;
	}

	@Override
	public Expression getSelection() {
		return (Expression) querySpec.getSelectClause().getSelection();
	}

	@Override
	public boolean isDistinct() {
		return querySpec.getSelectClause().isDistinct();
	}

	@Override
	public Class getResultType() {
		return querySpec.getSelectClause().getJavaType();
	}

	@Override
	public <U> Subquery<U> subquery(Class<U> type) {
		return getParent().subquery( type );
	}

	@Override
	public Predicate getRestriction() {
		return querySpec.getWhereClause().getPredicate();
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
}
