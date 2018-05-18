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
public class LikeSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SqmExpression matchExpression;
	private final SqmExpression pattern;
	private final SqmExpression escapeCharacter;

	public LikeSqmPredicate(
			SessionFactoryImplementor sessionFactory,
			SqmExpression matchExpression,
			SqmExpression pattern,
			SqmExpression escapeCharacter) {
		this( sessionFactory, matchExpression, pattern, escapeCharacter, false );
	}

	public LikeSqmPredicate(
			SessionFactoryImplementor sessionFactory,
			SqmExpression matchExpression,
			SqmExpression pattern,
			SqmExpression escapeCharacter, boolean negated) {
		super( sessionFactory, negated );
		this.matchExpression = matchExpression;
		this.pattern = pattern;
		this.escapeCharacter = escapeCharacter;
	}

	public LikeSqmPredicate(SessionFactoryImplementor sessionFactory, SqmExpression matchExpression, SqmExpression pattern) {
		this( sessionFactory, matchExpression, pattern, null );
	}

	public SqmExpression getMatchExpression() {
		return matchExpression;
	}

	public SqmExpression getPattern() {
		return pattern;
	}

	public SqmExpression getEscapeCharacter() {
		return escapeCharacter;
	}

	@Override
	public LikeSqmPredicate copy(SqmCopyContext context) {
		return new LikeSqmPredicate(
				sessionFactory,
				matchExpression.copy( context ),
				pattern.copy( context ),
				escapeCharacter.copy( context ),
				isNegated()
		);
	}

	@Override
	public SqmPredicate not() {
		return new LikeSqmPredicate(
				sessionFactory,
				matchExpression,
				pattern,
				escapeCharacter,
				!isNegated()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLikePredicate( this );
	}
}
