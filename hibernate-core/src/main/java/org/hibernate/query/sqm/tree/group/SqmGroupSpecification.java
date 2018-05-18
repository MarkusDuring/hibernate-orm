/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.group;

import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Christian Beikov
 */
public class SqmGroupSpecification {
	private final SqmExpression groupExpression;
	private final String collation;

	public SqmGroupSpecification(SqmExpression sortExpression, String collation) {
		this.groupExpression = sortExpression;
		this.collation = collation;
	}

	public SqmGroupSpecification copy(SqmCopyContext context) {
		return new SqmGroupSpecification(
				groupExpression.copy( context ),
				collation
		);
	}

	public SqmExpression getGroupExpression() {
		return groupExpression;
	}

	public String getCollation() {
		return collation;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(groupExpression);
		if ( collation != null ) {
			sb.append( collation );
		}
		return sb.toString();
	}
}
