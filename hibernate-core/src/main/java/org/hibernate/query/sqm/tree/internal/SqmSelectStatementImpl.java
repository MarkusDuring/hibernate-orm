/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.EntityType;

import org.hibernate.query.criteria.spi.JpaCriteriaBuilderImplementor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.select.SqmSelection;

/**
 * @author Steve Ebersole
 */
public class SqmSelectStatementImpl extends AbstractSqmStatement implements SqmSelectStatement {
	private SqmQuerySpec querySpec;

	public SqmSelectStatementImpl(
			JpaCriteriaBuilderImplementor criteriaBuilder,
			ParsingContext parsingContext) {
		super( criteriaBuilder, parsingContext );
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
	public CriteriaQuery select(Selection selection) {
		List<SqmSelection> sqmSelections = new ArrayList<>(  );
		sqmSelections.add( createSelection( selection ) );
		querySpec.getSelectClause().setSelections( sqmSelections );
		return this;
	}

	@Override
	public CriteriaQuery multiselect(Selection[] selections) {
		List<SqmSelection> sqmSelections = new ArrayList<>( selections.length );
		for (Selection s : selections) {
			sqmSelections.add( createSelection( s ) );
		}
		querySpec.getSelectClause().setSelections( sqmSelections );
		return this;
	}

	@Override
	public CriteriaQuery multiselect(List list) {
		return this;
	}

	@Override
	public CriteriaQuery where(Expression restriction) {
		setWhere( querySpec.getWhereClause(), restriction );
		return this;
	}

	@Override
	public CriteriaQuery where(Predicate... restrictions) {
		setWhere( querySpec.getWhereClause(), restrictions );
		return this;
	}

	@Override
	public CriteriaQuery groupBy(Expression[] grouping) {
		return this;
	}

	@Override
	public CriteriaQuery groupBy(List grouping) {
		return this;
	}

	@Override
	public CriteriaQuery having(Expression restriction) {
		return this;
	}

	@Override
	public CriteriaQuery having(Predicate... restrictions) {
		return this;
	}

	@Override
	public CriteriaQuery orderBy(Order... o) {
		return this;
	}

	@Override
	public CriteriaQuery orderBy(List o) {
		return this;
	}

	@Override
	public CriteriaQuery distinct(boolean distinct) {
		querySpec.getSelectClause().setDistinct(distinct);
		return this;
	}

	@Override
	public List<Order> getOrderList() {
		return null;
	}

	@Override
	public Set<ParameterExpression<?>> getParameters() {
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
	public Selection getSelection() {
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
		return resultType;
	}

	@Override
	public Predicate getRestriction() {
		if ( querySpec == null || querySpec.getWhereClause() == null ) {
			return null;
		}
		return querySpec.getWhereClause().getPredicate();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSelectStatement( this );
	}
}
