/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.predicate;

import org.hibernate.query.sqm.tree.expression.SqmExpression;

import javax.persistence.criteria.CriteriaBuilder;

/**
 * @author Steve Ebersole
 */
public interface InSqmPredicate extends NegatableSqmPredicate, CriteriaBuilder.In<Object> {
	SqmExpression getTestExpression();
}
