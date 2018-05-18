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
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;

/**
 * @author Steve Ebersole
 */
public class MemberOfSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SqmExpression expression;
	private final SqmPluralAttributeReference pluralAttributeReference;

	public MemberOfSqmPredicate(SessionFactoryImplementor sessionFactory, SqmExpression expression, SqmPluralAttributeReference pluralAttributeReference) {
		this( sessionFactory, expression, pluralAttributeReference, false );
	}

	public MemberOfSqmPredicate(SessionFactoryImplementor sessionFactory, SqmExpression expression, SqmPluralAttributeReference pluralAttributeReference, boolean negated) {
		super( sessionFactory, negated );

		assert expression != null;
		assert pluralAttributeReference != null;
		this.expression = expression;
		this.pluralAttributeReference = pluralAttributeReference;
	}

	public SqmExpression getExpression() {
		return expression;
	}

	public SqmPluralAttributeReference getPluralAttributeReference() {
		return pluralAttributeReference;
	}

	@Override
	public MemberOfSqmPredicate copy(SqmCopyContext context) {
		return new MemberOfSqmPredicate(
				sessionFactory,
				expression,
				pluralAttributeReference.copy( context ),
				isNegated()
		);
	}

	@Override
	public SqmPredicate not() {
		return new MemberOfSqmPredicate(
				sessionFactory,
				expression,
				pluralAttributeReference,
				!isNegated()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMemberOfPredicate( this );
	}
}
