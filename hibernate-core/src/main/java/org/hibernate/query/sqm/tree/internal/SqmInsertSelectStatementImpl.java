/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.internal;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmInsertSelectStatement;
import org.hibernate.query.sqm.tree.SqmNode;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReference;
import org.hibernate.query.sqm.tree.from.SqmRoot;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Ebersole
 */
public class SqmInsertSelectStatementImpl extends AbstractSqmInsertStatement implements SqmInsertSelectStatement {
	private SqmQuerySpec selectQuery;

	public SqmInsertSelectStatementImpl(SqmCreationContext creationContext, SqmRoot insertTarget) {
		super( creationContext, insertTarget );
	}

	public SqmInsertSelectStatementImpl(SqmCreationContext creationContext, SqmRoot insertTarget, List<SqmSingularAttributeReference> stateFields, SqmQuerySpec selectQuery) {
		super( creationContext, insertTarget, stateFields );
		this.selectQuery = selectQuery;
	}

	@Override
	public SqmInsertSelectStatement copy(SqmCopyContext context) {
		// First register the copy instance so that subqueries can look it up
		SqmInsertSelectStatementImpl statement = new SqmInsertSelectStatementImpl(
				context.getCreationContext(),
				getInsertTarget().copy( context ),
				null,
				null
		);
		// only then copy the query spec
		statement.selectQuery = selectQuery.copy( context );

		for ( SqmSingularAttributeReference stateField : getStateFields() ) {
			statement.addInsertTargetStateField( stateField.copy( context ) );
		}

		return statement;
	}

	@Override
	public SqmQuerySpec getSelectQuery() {
		return selectQuery;
	}

	public void setSelectQuery(SqmQuerySpec selectQuery) {
		this.selectQuery = selectQuery;
	}

	@Override
	public Predicate getRestriction() {
		return null;
	}
}
