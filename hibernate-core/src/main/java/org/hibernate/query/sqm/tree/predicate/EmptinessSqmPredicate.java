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
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;

/**
 * @author Steve Ebersole
 */
public class EmptinessSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SqmPluralAttributeReference expression;

	public EmptinessSqmPredicate(SessionFactoryImplementor sessionFactory, SqmPluralAttributeReference expression) {
		this( sessionFactory, expression, false );
	}

	public EmptinessSqmPredicate(SessionFactoryImplementor sessionFactory, SqmPluralAttributeReference expression, boolean negated) {
		super( sessionFactory, negated );
		this.expression = expression;
	}

	public SqmPluralAttributeReference getExpression() {
		return expression;
	}

	@Override
	public EmptinessSqmPredicate copy(SqmCopyContext context) {
		return new EmptinessSqmPredicate( sessionFactory, expression.copy( context ), isNegated() );
	}

	@Override
	public SqmPredicate not() {
		return new EmptinessSqmPredicate( sessionFactory, expression, !isNegated() );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitIsEmptyPredicate( this );
	}
}
