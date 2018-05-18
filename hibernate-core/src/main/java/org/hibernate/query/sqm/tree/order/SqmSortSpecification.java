/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.order;

import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;

/**
 * @author Steve Ebersole
 */
public class SqmSortSpecification implements Order {
	private final SqmExpression sortExpression;
	private final String collation;
	private final SqmSortOrder sortOrder;

	public SqmSortSpecification(SqmExpression sortExpression, String collation, SqmSortOrder sortOrder) {
		this.sortExpression = sortExpression;
		this.collation = collation;
		this.sortOrder = sortOrder;
	}

	public SqmSortSpecification(SqmExpression sortExpression) {
		this( sortExpression, null, null );
	}

	public SqmSortSpecification(SqmExpression sortExpression, SqmSortOrder sortOrder) {
		this( sortExpression, null, sortOrder );
	}

	public SqmSortSpecification copy(SqmCopyContext context) {
		return new SqmSortSpecification(
				sortExpression.copy( context ),
				collation,
				sortOrder
		);
	}

	public SqmExpression getSortExpression() {
		return sortExpression;
	}

	public String getCollation() {
		return collation;
	}

	public SqmSortOrder getSortOrder() {
		return sortOrder;
	}

	@Override
	public Order reverse() {
		return new SqmSortSpecification(
				sortExpression,
				collation,
				sortOrder == SqmSortOrder.DESCENDING ? SqmSortOrder.ASCENDING : SqmSortOrder.DESCENDING
		);
	}

	@Override
	public boolean isAscending() {
		return sortOrder != SqmSortOrder.DESCENDING;
	}

	@Override
	public Expression<?> getExpression() {
		return sortExpression;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( sortExpression );
		if ( sortOrder != null ) {
			sb.append( sortOrder );
		}
		if ( collation != null ) {
			sb.append( collation );
		}
		return sb.toString();
	}
}
