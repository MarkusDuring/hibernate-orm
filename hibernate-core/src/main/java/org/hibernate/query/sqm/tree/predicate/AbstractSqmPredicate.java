/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.predicate;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.tree.expression.*;
import org.hibernate.query.sqm.tree.expression.function.SqmCastFunction;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Selection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 */
public abstract class AbstractSqmPredicate implements SqmPredicate {

	protected final SessionFactoryImplementor sessionFactory;
	private String alias;

	public AbstractSqmPredicate(SessionFactoryImplementor sessionFactory) {
		assert sessionFactory != null;
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Class<Boolean> getJavaType() {
		return Boolean.class;
	}

	@Override
	public BooleanOperator getOperator() {
		return BooleanOperator.AND;
	}

	@Override
	public boolean isNegated() {
		return false;
	}

	@Override
	public SqmPredicate not() {
		return new NegatedSqmPredicate( sessionFactory, this );
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return Collections.emptyList();
	}

	private SqmExpression predicateExpression() {
		SqmCaseSearched caseSearched = new SqmCaseSearched( sessionFactory );
		caseSearched.when( this, sessionFactory.getCriteriaBuilder().conjunction() );
		caseSearched.otherwise( sessionFactory.getCriteriaBuilder().disjunction() );
		return caseSearched;
	}

	@Override
	public SqmPredicate isNull() {
		return (SqmPredicate) sessionFactory.getCriteriaBuilder().isNull( predicateExpression() );
	}

	@Override
	public SqmPredicate isNotNull() {
		return (SqmPredicate) sessionFactory.getCriteriaBuilder().isNotNull( predicateExpression() );
	}

	@Override
	public SqmPredicate in(Object... values) {
		return (SqmPredicate) sessionFactory.getCriteriaBuilder().in( predicateExpression(), values );
	}

	@Override
	public SqmPredicate in(Expression[] values) {
		return (SqmPredicate) sessionFactory.getCriteriaBuilder().in( predicateExpression(), values );
	}

	@Override
	public SqmPredicate in(Collection values) {
		return (SqmPredicate) sessionFactory.getCriteriaBuilder().in( predicateExpression(), values );
	}

	@Override
	public SqmPredicate in(Expression values) {
		return (SqmPredicate) sessionFactory.getCriteriaBuilder().in( predicateExpression() ).value( values );
	}

	@Override
	public <X> Expression<X> as(Class<X> type) {
		return type.equals( getJavaType() )
				? predicateExpression()
				: new SqmCastFunction(
					sessionFactory,
					predicateExpression(),
					sessionFactory.getTypeConfiguration()
						.getBasicTypeRegistry()
						.getBasicType(type)
		);
	}

	@Override
	public Selection<Boolean> alias(String name) {
		this.alias = name;
		return this;
	}

	@Override
	public boolean isCompoundSelection() {
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		throw new IllegalStateException( "not a compound selection" );
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
