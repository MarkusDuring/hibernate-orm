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
import java.util.List;

/**
 * @author Steve Ebersole
 */
public class NegatedSqmPredicate extends AbstractSqmPredicate implements SqmPredicate {
	private final SqmPredicate wrappedPredicate;

	public NegatedSqmPredicate(SessionFactoryImplementor sessionFactory, SqmPredicate wrappedPredicate) {
		super( sessionFactory );
		this.wrappedPredicate = wrappedPredicate;
	}

	public SqmPredicate getWrappedPredicate() {
		return wrappedPredicate;
	}

	@Override
	public NegatedSqmPredicate copy(SqmCopyContext context) {
		return new NegatedSqmPredicate( sessionFactory, wrappedPredicate.copy( context ) );
	}

	@Override
	public boolean isNegated() {
		return true;
	}

	@Override
	public SqmPredicate not() {
		return wrappedPredicate;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return wrappedPredicate.getExpressions();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitNegatedPredicate( this );
	}
}
