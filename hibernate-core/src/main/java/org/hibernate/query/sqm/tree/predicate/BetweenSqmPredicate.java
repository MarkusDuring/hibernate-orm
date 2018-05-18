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

/**
 * @author Steve Ebersole
 */
public class BetweenSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SqmExpression expression;
	private final SqmExpression lowerBound;
	private final SqmExpression upperBound;

	public BetweenSqmPredicate(
			SessionFactoryImplementor sessionFactory,
			SqmExpression expression,
			SqmExpression lowerBound,
			SqmExpression upperBound,
			boolean negated) {
		super( sessionFactory, negated );
		this.expression = expression;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public SqmExpression getExpression() {
		return expression;
	}

	public SqmExpression getLowerBound() {
		return lowerBound;
	}

	public SqmExpression getUpperBound() {
		return upperBound;
	}

	@Override
	public BetweenSqmPredicate copy(SqmCopyContext context) {
		return new BetweenSqmPredicate(
				sessionFactory,
				expression.copy( context ),
				lowerBound.copy( context ),
				upperBound.copy( context ),
				isNegated()
		);
	}

	@Override
	public SqmPredicate not() {
		return new BetweenSqmPredicate(
				sessionFactory,
				expression,
				lowerBound,
				upperBound,
				!isNegated()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitBetweenPredicate( this );
	}
}
