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

/**
 * @author Steve Ebersole
 */
public class GroupedSqmPredicate extends AbstractSqmPredicate implements SqmPredicate {
	private final SqmPredicate subPredicate;

	public GroupedSqmPredicate(SessionFactoryImplementor sessionFactory, SqmPredicate subPredicate) {
		super( sessionFactory );
		this.subPredicate = subPredicate;
	}

	public SqmPredicate getSubPredicate() {
		return subPredicate;
	}

	@Override
	public GroupedSqmPredicate copy(SqmCopyContext context) {
		return new GroupedSqmPredicate( sessionFactory, subPredicate.copy( context ) );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitGroupedPredicate( this );
	}
}
