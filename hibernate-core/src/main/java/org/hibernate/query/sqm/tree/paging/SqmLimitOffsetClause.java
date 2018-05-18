/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.paging;

import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Christian Beikov
 */
public class SqmLimitOffsetClause {
	private SqmExpression limitExpression;
	private SqmExpression offsetExpression;

	public SqmLimitOffsetClause() {
	}

	public SqmLimitOffsetClause(SqmExpression limitExpression, SqmExpression offsetExpression) {
		this.limitExpression = limitExpression;
		this.offsetExpression = offsetExpression;
	}

	public SqmLimitOffsetClause copy(SqmCopyContext context) {
		return new SqmLimitOffsetClause(
				limitExpression == null ? null : limitExpression.copy( context ),
				offsetExpression == null ? null : offsetExpression.copy( context )
		);
	}

	public SqmExpression getLimitExpression() {
		return limitExpression;
	}

	public SqmExpression getOffsetExpression() {
		return offsetExpression;
	}
}
