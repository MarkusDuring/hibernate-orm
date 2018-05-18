/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.predicate;

import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractNegatableSqmPredicate extends AbstractSqmPredicate implements NegatableSqmPredicate {
	private boolean negated;

	public AbstractNegatableSqmPredicate(SessionFactoryImplementor sessionFactory) {
		this( sessionFactory, false );
	}

	public AbstractNegatableSqmPredicate(SessionFactoryImplementor sessionFactory, boolean negated) {
		super( sessionFactory );
		this.negated = negated;
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public void negate() {
		this.negated = !this.negated;
	}
}
