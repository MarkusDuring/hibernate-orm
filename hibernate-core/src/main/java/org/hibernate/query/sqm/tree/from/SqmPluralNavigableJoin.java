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
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;

import javax.persistence.criteria.PluralJoin;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.PluralAttribute;

/**
 *
 * @author Christian Beikov
 */
public abstract class SqmPluralNavigableJoin extends SqmNavigableJoin {

	public SqmPluralNavigableJoin(
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
	public SqmPluralAttributeReference getNavigableReference() {
		return (SqmPluralAttributeReference) super.getNavigableReference();
	}

	@Override
	public PluralAttribute getModel() {
		return (PluralAttribute) super.getModel();
	}

}
