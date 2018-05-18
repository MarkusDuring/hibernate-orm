/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.set;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReference;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class SqmSetClause {
	private List<SqmAssignment> assignments = new ArrayList<>();

	public SqmSetClause() {
	}

	private SqmSetClause(List<SqmAssignment> assignments) {
		this.assignments = assignments;
	}

	public List<SqmAssignment> getAssignments() {
		return Collections.unmodifiableList( assignments );
	}

	public void addAssignment(SqmAssignment assignment) {
		assignments.add( assignment );
	}

	public void addAssignment(SqmSingularAttributeReference stateField, SqmExpression value) {
		addAssignment( new SqmAssignment( stateField, value ) );
	}

	public void copyFrom(SqmSetClause source, SqmCopyContext context) {
		List<SqmAssignment> newAssignments = new ArrayList<>( source.assignments.size() );
		for ( SqmAssignment assignment : source.assignments ) {
			newAssignments.add( assignment.copy( context ) );
		}

		this.assignments = newAssignments;
	}
}
