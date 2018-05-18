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
public class NullnessSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SqmExpression expression;

	public NullnessSqmPredicate(SessionFactoryImplementor sessionFactory, SqmExpression expression) {
		this( sessionFactory, expression, false );
	}

	public NullnessSqmPredicate(SessionFactoryImplementor sessionFactory, SqmExpression expression, boolean negated) {
		super( sessionFactory, negated );
		this.expression = expression;
	}

	public SqmExpression getExpression() {
		return expression;
	}

	@Override
	public NullnessSqmPredicate copy(SqmCopyContext context) {
		return new NullnessSqmPredicate( sessionFactory, expression.copy( context ), isNegated() );
	}

	@Override
	public SqmPredicate not() {
		return new NullnessSqmPredicate( sessionFactory, expression, !isNegated() );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitIsNullPredicate( this );
	}
}
