/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.select;

import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmVisitableNode;

import javax.persistence.criteria.Selection;
import java.util.List;

/**
 * Represents an individual selection within a select clause.
 *
 * @author Steve Ebersole
 */
public abstract class SqmSelectionBase implements SqmAliasedNode, SqmVisitableNode, JpaSelection {

	private String alias;

	protected SqmSelectionBase() {
	}

	protected SqmSelectionBase(String alias) {
		this.alias = alias;
	}

	@Override
	public abstract SqmSelectionBase copy(SqmCopyContext context);

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public Selection alias(String name) {
		this.alias = name;
		return this;
	}

	@Override
	public boolean isCompoundSelection() {
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		throw new IllegalStateException( "not a compound selection" );
	}
}
