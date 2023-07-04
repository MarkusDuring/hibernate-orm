/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.model.internal;

import java.util.List;
import java.util.function.BiConsumer;

import org.hibernate.sql.model.MutationTarget;
import org.hibernate.sql.model.MutationType;
import org.hibernate.sql.model.ast.MutationGroup;
import org.hibernate.sql.model.ast.TableMutation;

/**
 * Standard MutationGroup implementation for cases with multiple table mutations
 *
 * @author Steve Ebersole
 */
public class MutationGroupStandard implements MutationGroup {
	private final MutationType mutationType;
	private final MutationTarget<?> mutationTarget;
	private final List<? extends TableMutation<?>> tableMutationList;

	public MutationGroupStandard(
			MutationType mutationType,
			MutationTarget<?> mutationTarget,
			List<? extends TableMutation<?>> tableMutationList) {
		this.mutationType = mutationType;
		this.mutationTarget = mutationTarget;
		this.tableMutationList = tableMutationList;
	}

	@Override
	public MutationType getMutationType() {
		return mutationType;
	}

	@Override
	public MutationTarget<?> getMutationTarget() {
		return mutationTarget;
	}

	@Override
	public int getNumberOfTableMutations() {
		return tableMutationList.size();
	}

	@Override
	public TableMutation getSingleTableMutation() {
		throw new IllegalStateException( "Group contains multiple table mutations : " + mutationTarget.getNavigableRole() );
	}

	@Override
	public TableMutation getTableMutation(String tableName) {
		for ( int i = 0; i < tableMutationList.size(); i++ ) {
			final TableMutation<?> tableMutation = tableMutationList.get( i );
			if ( tableMutation != null ) {
				if ( tableMutation.getMutatingTable().getTableName().equals( tableName ) ) {
					return tableMutation;
				}
			}
		}
		return null;
	}

	@Override
	public void forEachTableMutation(BiConsumer<Integer, TableMutation> action) {
		for ( int i = 0; i < tableMutationList.size(); i++ ) {
			action.accept( i, tableMutationList.get( i ) );
		}
	}

}
