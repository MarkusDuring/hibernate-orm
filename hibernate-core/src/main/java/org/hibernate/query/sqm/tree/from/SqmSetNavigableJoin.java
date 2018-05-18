/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.SetAttribute;

/**
 *
 * @author Christian Beikov
 */
public class SqmSetNavigableJoin extends SqmPluralNavigableJoin implements SetJoin {

	public SqmSetNavigableJoin(
			SqmFrom lhs,
			SqmNavigableReference navigableReference,
			String uid,
			String alias,
			SqmJoinType joinType,
			boolean fetched,
			SqmCreationContext creationContext) {
		super(
				lhs,
				navigableReference,
				uid,
				alias,
				joinType,
				fetched,
				creationContext
		);
	}

	@Override
	public SetAttribute getModel() {
		return (SetAttribute) super.getModel();
	}

	@Override
	public SetJoin on(Expression restriction) {
		return (SetJoin) super.on(restriction);
	}

	@Override
	public SetJoin on(Predicate... restrictions) {
		return (SetJoin) super.on(restrictions);
	}

}
