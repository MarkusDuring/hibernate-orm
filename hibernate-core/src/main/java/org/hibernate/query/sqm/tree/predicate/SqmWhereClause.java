/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.predicate;

import org.hibernate.query.sqm.tree.SqmCopyContext;
/**
 * @author Steve Ebersole
 */
public class SqmWhereClause {
	private SqmPredicate predicate;

	public SqmWhereClause() {
	}

	public SqmWhereClause(SqmPredicate predicate) {
		this.predicate = predicate;
	}

	public SqmPredicate getPredicate() {
		return predicate;
	}

	public void setPredicate(SqmPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public String toString() {
		return "where " + predicate;
	}

	public SqmWhereClause copy(SqmCopyContext context) {
		return new SqmWhereClause( predicate == null ? null : predicate.copy( context ) );
	}

	public void copyFrom(SqmWhereClause source, SqmCopyContext context) {
		this.predicate = source.predicate == null ? null : source.predicate.copy( context );
	}
}
