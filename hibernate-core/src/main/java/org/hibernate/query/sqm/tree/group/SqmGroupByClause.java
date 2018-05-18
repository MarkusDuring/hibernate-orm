/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.group;

import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 */
public class SqmGroupByClause {
	private List<SqmGroupSpecification> groupBySpecifications;

	public SqmGroupByClause() {
	}

	private SqmGroupByClause(List<SqmGroupSpecification> groupBySpecifications) {
		this.groupBySpecifications = groupBySpecifications;
	}

	public SqmGroupByClause copy(SqmCopyContext context) {
		List<SqmGroupSpecification> newGroupByItems = null;
		if ( groupBySpecifications != null ) {
			newGroupByItems = new ArrayList<>( groupBySpecifications.size() );
			for ( SqmGroupSpecification groupBySpecification : groupBySpecifications) {
				newGroupByItems.add( groupBySpecification.copy( context ) );
			}
		}
		return new SqmGroupByClause( newGroupByItems );
	}

	public SqmGroupByClause addGroupBySpecification(SqmExpression expression) {
		addGroupBySpecification( new SqmGroupSpecification( expression, null ) );
		return this;
	}

	public SqmGroupByClause addGroupBySpecification(SqmGroupSpecification specification) {
		if ( groupBySpecifications == null ) {
			groupBySpecifications = new ArrayList<>();
		}
		groupBySpecifications.add( specification );
		return this;
	}

	public SqmGroupByClause setGroupBySpecifications(List<SqmGroupSpecification> groupBySpecifications) {
		if ( this.groupBySpecifications == null ) {
			this.groupBySpecifications = new ArrayList<>( groupBySpecifications.size() );
		} else {
			this.groupBySpecifications.clear();
		}
		this.groupBySpecifications.addAll(groupBySpecifications);
		return this;
	}

	public List<SqmGroupSpecification> getGroupBySpecifications() {
		if ( groupBySpecifications == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList(groupBySpecifications);
		}
	}

	@Override
	public String toString() {
		return "group by " + String.join(
				", ",
				groupBySpecifications.stream()
				.map(
						item -> (CharSequence) item.toString()
				)::iterator
		);
	}
}
