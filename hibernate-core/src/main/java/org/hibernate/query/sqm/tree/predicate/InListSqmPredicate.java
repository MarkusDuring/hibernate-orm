/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.predicate;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.SqmSubQuery;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Steve Ebersole
 */
public class InListSqmPredicate extends AbstractNegatableSqmPredicate implements InSqmPredicate {
	private final SqmExpression testExpression;
	private final List<SqmExpression> listExpressions;
	private boolean containsSubQuery;

	public InListSqmPredicate(SessionFactoryImplementor sessionFactory, SqmExpression testExpression) {
		this( sessionFactory, testExpression, new ArrayList<>() );
	}

	public InListSqmPredicate(SessionFactoryImplementor sessionFactory, SqmExpression testExpression, SqmSubQuery subquery) {
		this( sessionFactory, testExpression, new ArrayList<>( Arrays.asList( subquery ) ) );
	}

	public InListSqmPredicate(SessionFactoryImplementor sessionFactory, SqmExpression testExpression, SqmExpression... listExpressions) {
		this( sessionFactory, testExpression, new ArrayList<>( Arrays.asList( listExpressions ) ) );
	}

	public InListSqmPredicate(
			SessionFactoryImplementor sessionFactory,
			SqmExpression testExpression,
			List<SqmExpression> listExpressions) {
		this( sessionFactory, testExpression, listExpressions, false );
	}

	public InListSqmPredicate(
			SessionFactoryImplementor sessionFactory,
			SqmExpression testExpression,
			List<SqmExpression> listExpressions,
			boolean negated) {
		super( sessionFactory, negated );
		this.testExpression = testExpression;
		this.listExpressions = listExpressions;
	}

	public InListSqmPredicate(
			SessionFactoryImplementor sessionFactory,
			SqmExpression testExpression,
			List<SqmExpression> listExpressions,
			boolean negated,
			boolean containsSubQuery) {
		super( sessionFactory, negated );
		this.testExpression = testExpression;
		this.listExpressions = listExpressions;
		this.containsSubQuery = containsSubQuery;
	}

	@Override
	public SqmExpression getTestExpression() {
		return testExpression;
	}

	public List<SqmExpression> getListExpressions() {
		return listExpressions;
	}

	public void addExpression(SqmExpression expression) {
		if ( expression instanceof SqmSubQuery ) {
			this.containsSubQuery = true;
		}
		// todo 6.0 : check for JPA compliance and give a warning if subquery + values are mixed
		listExpressions.add( expression );
	}

	@Override
	public Expression getExpression() {
		return testExpression;
	}

	@Override
	public CriteriaBuilder.In value(Object value) {
		addExpression( (SqmExpression) sessionFactory.getCriteriaBuilder().literal( value ) );
		return this;
	}

	@Override
	public CriteriaBuilder.In value(Expression value) {
		addExpression( (SqmExpression) value );
		return this;
	}

	@Override
	public InListSqmPredicate copy(SqmCopyContext context) {
		List<SqmExpression> newListExpressions = new ArrayList<>( listExpressions.size() );
		for ( SqmExpression expression : listExpressions ) {
			newListExpressions.add( expression.copy( context ) );
		}

		return new InListSqmPredicate(
				sessionFactory,
				testExpression.copy( context ),
				newListExpressions,
				isNegated(),
				containsSubQuery
		);
	}

	@Override
	public SqmPredicate not() {
		return new InListSqmPredicate(
				sessionFactory,
				testExpression,
				listExpressions,
				!isNegated(),
				containsSubQuery
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitInListPredicate( this );
	}
}
