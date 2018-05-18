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

import javax.persistence.criteria.*;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.PluralAttribute;

/**
 *
 * @author Christian Beikov
 */
public class SqmCollectionNavigableJoin extends SqmPluralNavigableJoin implements CollectionJoin {

	public SqmCollectionNavigableJoin(
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
	public CollectionAttribute getModel() {
		return (CollectionAttribute) super.getModel();
	}

	@Override
	public CollectionJoin on(Expression restriction) {
		return (CollectionJoin) super.on(restriction);
	}

	@Override
	public CollectionJoin on(Predicate... restrictions) {
		return (CollectionJoin) super.on(restrictions);
	}
}
