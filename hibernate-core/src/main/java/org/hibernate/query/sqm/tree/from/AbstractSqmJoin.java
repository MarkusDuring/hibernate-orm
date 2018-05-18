/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmJoinType;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmJoin extends AbstractSqmFrom implements SqmJoin {
	private final SqmJoinType joinType;

	public AbstractSqmJoin(
			SqmFromElementSpace fromElementSpace,
			String uid,
			String alias,
			SqmJoinType joinType,
			SqmCreationContext creationContext) {
		super( fromElementSpace, uid, alias, creationContext );
		this.joinType = joinType;
	}

	@Override
	public abstract AbstractSqmJoin copy(SqmCopyContext context);

	@Override
	public SqmJoinType getSqmJoinType() {
		return joinType;
	}
}
