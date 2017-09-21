/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.sql.results.spi.DynamicInstantiationQueryResult;
import org.hibernate.sql.results.spi.InitializerCollector;
import org.hibernate.sql.results.spi.QueryResultAssembler;

/**
 * @author Steve Ebersole
 */
public class DynamicInstantiationQueryResultImpl implements DynamicInstantiationQueryResult {
	private final String resultVariable;
	private final QueryResultAssembler assembler;

	public DynamicInstantiationQueryResultImpl(
			String resultVariable,
			QueryResultAssembler assembler) {
		this.resultVariable = resultVariable;
		this.assembler = assembler;
	}

	@Override
	public String getResultVariable() {
		return resultVariable;
	}

	@Override
	public void registerInitializers(InitializerCollector collector) {
		// none to register specifically - although we need to be able to register
		// initializers from any of the arguments
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public QueryResultAssembler getResultAssembler() {
		return assembler;
	}
}
