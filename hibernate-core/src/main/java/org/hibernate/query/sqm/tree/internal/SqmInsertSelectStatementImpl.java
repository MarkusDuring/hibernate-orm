/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.internal;

import org.hibernate.query.criteria.spi.JpaCriteriaBuilderImplementor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.tree.SqmInsertSelectStatement;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.from.SqmRoot;

/**
 * @author Steve Ebersole
 */
public class SqmInsertSelectStatementImpl extends AbstractSqmInsertStatement implements SqmInsertSelectStatement {
	private SqmQuerySpec selectQuery;

	public SqmInsertSelectStatementImpl(
			JpaCriteriaBuilderImplementor criteriaBuilder,
			ParsingContext parsingContext) {
		super( criteriaBuilder, parsingContext );
	}

	public SqmInsertSelectStatementImpl(
			JpaCriteriaBuilderImplementor criteriaBuilder,
			ParsingContext parsingContext,
			SqmRoot insertTarget) {
		super( criteriaBuilder, parsingContext, insertTarget );
	}

	@Override
	public SqmQuerySpec getSelectQuery() {
		return selectQuery;
	}

	public void setSelectQuery(SqmQuerySpec selectQuery) {
		this.selectQuery = selectQuery;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitInsertSelectStatement( this );
	}
}
