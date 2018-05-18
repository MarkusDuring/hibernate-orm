/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.select;

import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;
import java.util.*;

/**
 * The semantic select clause.  Defined as a list of individual selections.
 *
 * @author Steve Ebersole
 */
public class SqmSelectClause implements SqmAliasedExpressionContainer<SqmSelectionBase>, CompoundSelection {
	private boolean distinct;
	private Class<?> javaType;
	private List<SqmSelectionBase> selections;

	public SqmSelectClause(boolean distinct) {
		this.distinct = distinct;
		this.javaType = Object[].class;
	}

	public SqmSelectClause(boolean distinct, Class<?> javaType) {
		this.distinct = distinct;
		this.javaType = javaType;
	}

	public SqmSelectClause(Class<?> javaType, List<SqmSelectionBase> selections) {
		this.distinct = false;
		this.javaType = javaType;
		this.selections = selections;
	}

	public SqmSelectClause(boolean distinct, Class<?> javaType, List<SqmSelectionBase> selections) {
		this.distinct = distinct;
		this.javaType = javaType;
		this.selections = selections;
	}

	public SqmSelectClause copy(SqmCopyContext context) {
		List<SqmSelectionBase> newSelections = null;
		if ( selections != null ) {
			newSelections = new ArrayList<>( selections.size() );
			for ( SqmSelectionBase selection : selections ) {
				newSelections.add( selection.copy( context ) );
			}
		}
		return new SqmSelectClause(
				distinct,
				javaType,
				newSelections
		);
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public List<SqmSelectionBase> getSelections() {
		if ( selections == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( selections );
		}
	}

	private void clearSelections() {
		if ( selections == null ) {
			selections = new ArrayList<>();
		} else {
			selections.clear();
		}
	}

	public void setSelection(SqmSelectionBase selection) {
        checkSelection( selection );
		clearSelections();
		selections.add( selection );
	}

	public void setSelection(SqmSelectClause selectClause) {
        checkSelection( selectClause );
		clearSelections();
		javaType = selectClause.getJavaType();
		selections.addAll( selectClause.getSelections() );
	}

    private void checkSelection(Selection<?> selection) {
        if ( selection instanceof CompoundSelection<?> ) {
            checkSelection( selection, new HashSet<>( selection.getCompoundSelectionItems().size() ) );
        }
    }

    private void checkSelection(Selection<?> selection, Set<String> aliases) {
        for ( Selection<?> selectionItem : selection.getCompoundSelectionItems() ) {
            if ( selectionItem.getAlias() != null && !aliases.add( selectionItem.getAlias() ) ) {
                throw new IllegalArgumentException( "duplicate alias in select: " + selectionItem.getAlias() );
            }

            if ( selectionItem instanceof CompoundSelection<?> ) {
				checkSelection( selectionItem, aliases );
			}
        }
    }

	public void addSelection(SqmSelectionBase selection) {
		if ( selections == null ) {
			selections = new ArrayList<>();
		}
		selections.add( selection );
	}

	public Selection<?> getSelection() {
		if ( javaType.isArray() ) {
			return this;
		} else if ( selections.isEmpty() ) {
			return null;
		} else {
			return selections.get( 0 );
		}
	}

	@Override
	public SqmSelectionBase add(SqmExpression expression, String alias) {
		final SqmSelectionBase selection = (SqmSelectionBase) expression;
		selection.alias( alias );
		addSelection( selection );
		return selection;
	}

	@Override
	public void add(SqmSelectionBase aliasExpression) {
		addSelection( aliasExpression );
	}

	@Override
	public Selection alias(String name) {
		return this;
	}

	@Override
	public boolean isCompoundSelection() {
		return true;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		return (List<Selection<?>>) (List) getSelections();
	}

	@Override
	public Class getJavaType() {
		return javaType;
	}

	@Override
	public String getAlias() {
		return null;
	}
}
