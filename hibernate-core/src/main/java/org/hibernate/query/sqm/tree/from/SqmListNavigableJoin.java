/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionIndexReferenceBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;

/**
 *
 * @author Christian Beikov
 */
public class SqmListNavigableJoin extends SqmPluralNavigableJoin implements ListJoin {

	public SqmListNavigableJoin(
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
	public ListAttribute getModel() {
		return (ListAttribute) super.getModel();
	}

	@Override
	public ListJoin on(Expression restriction) {
		return (ListJoin) super.on(restriction);
	}

	@Override
	public ListJoin on(Predicate... restrictions) {
		return (ListJoin) super.on(restrictions);
	}

	@Override
	public Expression<Integer> index() {
		return getNavigableReference().getReferencedNavigable()
				.getPersistentCollectionDescriptor()
				.getIndexDescriptor()
				.createSqmExpression( this, getNavigableReference(), getCreationContext() );
	}
}
