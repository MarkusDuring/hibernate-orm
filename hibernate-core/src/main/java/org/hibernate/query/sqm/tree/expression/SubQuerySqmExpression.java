/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

import org.hibernate.query.criteria.internal.expression.function.CastFunction;
import org.hibernate.query.criteria.spi.JpaCriteriaBuilderImplementor;
import org.hibernate.query.criteria.spi.JpaExpressionImplementor;
import org.hibernate.query.criteria.spi.JpaPredicateImplementor;
import org.hibernate.query.criteria.spi.JpaSelectionImplementor;
import org.hibernate.query.criteria.spi.JpaSubquery;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.tree.expression.function.SqmCastFunction;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.internal.AbstractSqmStatement;
import org.hibernate.query.sqm.tree.select.SqmSelection;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;

/**
 * @author Steve Ebersole
 */
public class SubQuerySqmExpression extends AbstractSqmStatement implements SqmExpression, JpaSubquery {
	private final AbstractQuery<?> parent;
	private final SqmQuerySpec querySpec;
	private final ExpressableType expressableType;

	public SubQuerySqmExpression(
			JpaCriteriaBuilderImplementor criteriaBuilder,
			ParsingContext parsingContext,
			AbstractQuery<?> parent, SqmQuerySpec querySpec, ExpressableType expressableType) {
		super( criteriaBuilder, parsingContext );
		this.parent = parent;
		this.querySpec = querySpec;
		this.expressableType = expressableType;
	}

	@Override
	public ExpressableType getExpressionType() {
		return expressableType;
	}

	@Override
	public ExpressableType getInferableType() {
		return getExpressionType();
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
	public Subquery select(Expression expression) {
		List<SqmSelection> sqmSelections = new ArrayList<>(  );
		sqmSelections.add( createSelection( expression ) );
		querySpec.getSelectClause().setSelections( sqmSelections );
		return this;
	}

	@Override
	public Subquery where(Expression restriction) {
		setWhere( querySpec.getWhereClause(), restriction );
		return this;
	}

	@Override
	public Subquery where(Predicate... restrictions) {
		setWhere( querySpec.getWhereClause(), restrictions );
		return this;
	}

	@Override
	public Subquery groupBy(Expression[] grouping) {
		return null;
	}

	@Override
	public Subquery groupBy(List grouping) {
		return null;
	}

	@Override
	public Subquery having(Expression restriction) {
		return null;
	}

	@Override
	public Subquery having(Predicate... restrictions) {
		return null;
	}

	@Override
	public Subquery distinct(boolean distinct) {
		querySpec.getSelectClause().setDistinct(distinct);
		return this;
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
		return parent;
	}

	@Override
	public CommonAbstractCriteria getContainingQuery() {
		return parent;
	}

	@Override
	public Expression getSelection() {
		List<SqmSelection> selections = querySpec.getSelectClause().getSelections();
		if (selections.isEmpty()) {
			return null;
		}
		return selections.get( 0 );
	}

	@Override
	public Set<Join<?, ?>> getCorrelatedJoins() {
		return null;
	}

	@Override
	public Root from(Class entityClass) {
		SqmRoot root = createRoot( entityClass );
		querySpec.getFromClause().makeFromElementSpace().setRoot( root );
		return root;
	}

	@Override
	public Root from(EntityType entity) {
		SqmRoot root = createRoot( entity );
		querySpec.getFromClause().makeFromElementSpace().setRoot( root );
		return root;
	}

	@Override
	public Set<Root<?>> getRoots() {
		return null;
	}

	@Override
	public List<Expression<?>> getGroupList() {
		return null;
	}

	@Override
	public Predicate getGroupRestriction() {
		return null;
	}

	@Override
	public boolean isDistinct() {
		return querySpec.getSelectClause().isDistinct();
	}

	@Override
	public Class getResultType() {
		return expressableType.getJavaType();
	}

	@Override
	public JpaPredicateImplementor getRestriction() {
		return querySpec.getWhereClause().getPredicate();
	}

	@Override
	public JpaPredicateImplementor isNull() {
		return getCriteriaBuilder().isNull( this );
	}

	@Override
	public JpaPredicateImplementor isNotNull() {
		return getCriteriaBuilder().isNotNull( this );
	}

	@Override
	public JpaPredicateImplementor in(Object... values) {
		return getCriteriaBuilder().in( this, values );
	}

	@Override
	public JpaPredicateImplementor in(Expression[] values) {
		return getCriteriaBuilder().in( this, values );
	}

	@Override
	public JpaPredicateImplementor in(Collection values) {
		return getCriteriaBuilder().in( this, values.toArray() );
	}

	@Override
	public JpaPredicateImplementor in(Expression values) {
		return getCriteriaBuilder().in( this, values );
	}

	@Override
	public JpaExpressionImplementor as(Class type) {
		return type.equals( expressableType.getJavaType() ) ? this : new SqmCastFunction(
				getCriteriaBuilder(),
				type,
				this
		);
	}

	@Override
	public JpaSelectionImplementor alias(String name) {
		return null;
	}

	@Override
	public boolean isCompoundSelection() {
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		return null;
	}

	@Override
	public Class getJavaType() {
		return expressableType.getJavaType();
	}

	@Override
	public String getAlias() {
		return null;
	}

	@Override
	public QueryResult createQueryResult(
			org.hibernate.sql.ast.tree.spi.expression.Expression expression,
			String resultVariable,
			QueryResultCreationContext creationContext) {
		return null;
	}
}
