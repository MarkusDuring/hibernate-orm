/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;

/**
 * Common contract for qualified/restricted/predicated joins.
 *
 * @author Steve Ebersole
 */
public interface SqmQualifiedJoin extends SqmJoin, Join {
	/**
	 * Obtain the join predicate
	 *
	 * @return The join predicate
	 */
	SqmPredicate getOnClausePredicate();

	/**
	 * Inject the join predicate
	 *
	 * @param predicate The join predicate
	 */
	void setOnClausePredicate(SqmPredicate predicate);

	// todo : specialized Predicate for "mapped attribute join" conditions

	HibernateCriteriaBuilder getCriteriaBuilder();

	@Override
	default Join on(Expression restriction) {
		setOnClausePredicate( (SqmPredicate) getCriteriaBuilder().wrap( restriction ) );
		return this;
	}

	@Override
	default Join on(Predicate... restrictions) {
		setOnClausePredicate( (SqmPredicate) getCriteriaBuilder().and( restrictions ) );
		return this;
	}

	@Override
	default Predicate getOn() {
		return getOnClausePredicate();
	}

	@Override
	default Attribute getAttribute() {
		return (Attribute) getNavigableReference().getReferencedNavigable();
	}

	@Override
	default JoinType getJoinType() {
		return getSqmJoinType().getCorrespondingJpaJoinType();
	}
}
