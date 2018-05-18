/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class SqmOrderByClause {
	private List<SqmSortSpecification> sortSpecifications;

	public SqmOrderByClause() {
	}

	public SqmOrderByClause(List<SqmSortSpecification> sortSpecifications) {
		this.sortSpecifications = sortSpecifications;
	}

	public SqmOrderByClause copy(SqmCopyContext context) {
		List<SqmSortSpecification> newSortSpecifications = null;
		if ( sortSpecifications != null ) {
			newSortSpecifications = new ArrayList<>( sortSpecifications.size() );
			for ( SqmSortSpecification sortSpecification : sortSpecifications ) {
				newSortSpecifications.add( sortSpecification.copy( context ) );
			}

		}
		return new SqmOrderByClause( newSortSpecifications );
	}

	public SqmOrderByClause addSortSpecification(SqmSortSpecification sortSpecification) {
		if ( sortSpecifications == null ) {
			sortSpecifications = new ArrayList<>();
		}
		sortSpecifications.add( sortSpecification );
		return this;
	}

	public SqmOrderByClause setSortSpecifications(List<SqmSortSpecification> sortSpecifications) {
		if ( this.sortSpecifications == null ) {
			this.sortSpecifications = new ArrayList<>( sortSpecifications.size() );
		} else {
			this.sortSpecifications.clear();
		}
		this.sortSpecifications.addAll( sortSpecifications );
		return this;
	}

	public List<SqmSortSpecification> getSortSpecifications() {
		if ( sortSpecifications == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( sortSpecifications );
		}
	}

	@Override
	public String toString() {
		return "order by " + String.join(
				", ",
				sortSpecifications.stream()
						.map(
								item -> (CharSequence) item.toString()
						)::iterator
		);
	}
}
