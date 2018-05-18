/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree;

import org.hibernate.query.sqm.tree.from.SqmFromClauseContainer;
import org.hibernate.query.sqm.tree.group.SqmGroupByClause;
import org.hibernate.query.sqm.tree.order.SqmOrderByClause;
import org.hibernate.query.sqm.tree.predicate.SqmHavingClause;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;
import org.hibernate.query.sqm.tree.select.SqmSelectClause;
import org.hibernate.query.sqm.tree.from.SqmFromClause;
import org.hibernate.query.sqm.tree.paging.SqmLimitOffsetClause;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClauseContainer;

/**
 * Defines the commonality between a root query and a subquery.
 *
 * @author Steve Ebersole
 */
public class SqmQuerySpec implements SqmFromClauseContainer, SqmWhereClauseContainer {
	private final SqmFromClause fromClause;
	private final SqmSelectClause selectClause;
	private final SqmWhereClause whereClause;
	private final SqmGroupByClause groupByClause;
	private final SqmHavingClause havingClause;
	private final SqmOrderByClause orderByClause;
	private final SqmLimitOffsetClause limitOffsetClause;

	// todo : group-by + having

	public SqmQuerySpec(
			SqmFromClause fromClause,
			SqmSelectClause selectClause,
			SqmWhereClause whereClause,
			SqmGroupByClause groupByClause,
			SqmHavingClause havingClause,
			SqmOrderByClause orderByClause,
			SqmLimitOffsetClause limitOffsetClause) {
		this.fromClause = fromClause;
		this.selectClause = selectClause;
		this.whereClause = whereClause;
		this.groupByClause = groupByClause;
		this.havingClause = havingClause;
		this.orderByClause = orderByClause;
		this.limitOffsetClause = limitOffsetClause;
	}

	public SqmQuerySpec copy(SqmCopyContext context) {
		return new SqmQuerySpec(
				fromClause.copy( context ),
				selectClause.copy( context ),
				whereClause.copy( context ),
				groupByClause.copy( context ),
				havingClause.copy( context ),
				orderByClause.copy( context ),
				limitOffsetClause.copy( context )
		);
	}

	public SqmSelectClause getSelectClause() {
		return selectClause;
	}

	@Override
	public SqmFromClause getFromClause() {
		return fromClause;
	}

	public SqmWhereClause getWhereClause() {
		return whereClause;
	}

	public SqmGroupByClause getGroupByClause() {
		return groupByClause;
	}

	public SqmHavingClause getHavingClause() {
		return havingClause;
	}

	public SqmOrderByClause getOrderByClause() {
		return orderByClause;
	}

	public SqmLimitOffsetClause getLimitOffsetClause() {
		return limitOffsetClause;
	}
}
