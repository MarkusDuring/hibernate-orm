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

import javax.persistence.criteria.Expression;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Ebersole
 */
public class AndSqmPredicate extends AbstractSqmPredicate implements SqmPredicate {
	private final SqmPredicate leftHandPredicate;
	private final SqmPredicate rightHandPredicate;

	public AndSqmPredicate(SessionFactoryImplementor sessionFactory, SqmPredicate leftHandPredicate, SqmPredicate rightHandPredicate) {
		super( sessionFactory );
		this.leftHandPredicate = leftHandPredicate;
		this.rightHandPredicate = rightHandPredicate;
	}

	public SqmPredicate getLeftHandPredicate() {
		return leftHandPredicate;
	}

	public SqmPredicate getRightHandPredicate() {
		return rightHandPredicate;
	}

	@Override
	public AndSqmPredicate copy(SqmCopyContext context) {
		return new AndSqmPredicate( sessionFactory, leftHandPredicate.copy( context ), rightHandPredicate.copy( context ) );
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		List<Expression<Boolean>> expressions = new ArrayList<>(2);

		List<Expression<Boolean>> leftHandExpressions = leftHandPredicate.getExpressions();
		if (leftHandExpressions.isEmpty()) {
			expressions.add(leftHandPredicate);
		} else {
			expressions.addAll(leftHandExpressions);
		}

		List<Expression<Boolean>> rightHandExpressions = rightHandPredicate.getExpressions();
		if (rightHandExpressions.isEmpty()) {
			expressions.add(rightHandPredicate);
		} else {
			expressions.addAll(rightHandExpressions);
		}

		return expressions;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitAndPredicate( this );
	}
}
