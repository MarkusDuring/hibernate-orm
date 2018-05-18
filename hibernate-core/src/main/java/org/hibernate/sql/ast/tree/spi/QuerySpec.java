/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.sql.ast.tree.spi.from.FromClause;
import org.hibernate.sql.ast.tree.spi.group.GroupSpecification;
import org.hibernate.sql.ast.tree.spi.predicate.Junction;
import org.hibernate.sql.ast.tree.spi.predicate.Predicate;
import org.hibernate.sql.ast.tree.spi.select.SelectClause;
import org.hibernate.sql.ast.tree.spi.sort.SortSpecification;

/**
 * @author Steve Ebersole
 */
public class QuerySpec implements SqlAstNode {
	private final boolean isRoot;

	private final FromClause fromClause = new FromClause();
	private final SelectClause selectClause = new SelectClause();

	private Predicate whereClauseRestrictions;
	private List<GroupSpecification> groupSpecifications;
	private Predicate havingClauseRestrictions;
	private List<SortSpecification> sortSpecifications;
	private Expression limitClauseExpression;
	private Expression offsetClauseExpression;

	public QuerySpec(boolean isRoot) {
		this.isRoot = isRoot;
	}

	/**
	 * Does this QuerySpec map to the statement's root query (as
	 * opposed to one of its sub-queries)?
	 */
	public boolean isRoot() {
		return isRoot;
	}

	public FromClause getFromClause() {
		return fromClause;
	}

	public SelectClause getSelectClause() {
		return selectClause;
	}

	public Predicate getWhereClauseRestrictions() {
		return whereClauseRestrictions;
	}

	public void setWhereClauseRestrictions(Predicate whereClauseRestrictions) {
		if ( this.whereClauseRestrictions != null ) {
			throw new UnsupportedOperationException( "Cannot set where-clause restrictions after already set; try #addRestriction" );
		}
		this.whereClauseRestrictions = whereClauseRestrictions;
	}

	public void addRestriction(Predicate predicate) {
		whereClauseRestrictions = addRestriction( whereClauseRestrictions, predicate );
	}

	public List<GroupSpecification> getGroupSpecifications() {
		if ( groupSpecifications == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( groupSpecifications );
		}
	}

	public void addGroupSpecification(GroupSpecification groupSpecification) {
		if ( groupSpecifications == null ) {
			groupSpecifications = new ArrayList<GroupSpecification>();
		}
		groupSpecifications.add( groupSpecification );
	}

	public Predicate getHavingClauseRestrictions() {
		return havingClauseRestrictions;
	}

	public void setHavingClauseRestrictions(Predicate havingClauseRestrictions) {
		if ( this.havingClauseRestrictions != null ) {
			throw new UnsupportedOperationException( "Cannot set having-clause restrictions after already set; try #addHavingRestriction" );
		}
		this.havingClauseRestrictions = havingClauseRestrictions;
	}

	public void addHavingRestriction(Predicate predicate) {
		havingClauseRestrictions = addRestriction( havingClauseRestrictions, predicate );
	}

	private Predicate addRestriction(Predicate existingRestrictions, Predicate predicate) {
		if ( existingRestrictions == null ) {
			existingRestrictions = predicate;
		}
		else if ( existingRestrictions instanceof Junction
				&& ( (Junction) existingRestrictions ).getNature() == Junction.Nature.CONJUNCTION ) {
			( (Junction) existingRestrictions ).add( predicate );
		}
		else {
			final Junction conjunction = new Junction( Junction.Nature.CONJUNCTION );
			conjunction.add( existingRestrictions );
			conjunction.add( predicate );
			existingRestrictions = conjunction;
		}

		return existingRestrictions;
	}

	public List<SortSpecification> getSortSpecifications() {
		if ( sortSpecifications == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( sortSpecifications );
		}
	}

	public void addSortSpecification(SortSpecification sortSpecification) {
		if ( sortSpecifications == null ) {
			sortSpecifications = new ArrayList<SortSpecification>();
		}
		sortSpecifications.add( sortSpecification );
	}

	public Expression getLimitClauseExpression() {
		return limitClauseExpression;
	}

	public void setLimitClauseExpression(Expression limitExpression) {
		if ( this.limitClauseExpression != null ) {
			throw new UnsupportedOperationException( "Cannot set limit-clause expression after already set" );
		}
		this.limitClauseExpression = limitExpression;
	}

	public Expression getOffsetClauseExpression() {
		return offsetClauseExpression;
	}

	public void setOffsetClauseExpression(Expression offsetExpression) {
		if ( this.offsetClauseExpression != null ) {
			throw new UnsupportedOperationException( "Cannot set offset-clause expression after already set" );
		}
		this.offsetClauseExpression = offsetExpression;
	}

	@Override
	public void accept(SqlAstWalker sqlTreeWalker) {
		sqlTreeWalker.visitQuerySpec( this );
	}
}
