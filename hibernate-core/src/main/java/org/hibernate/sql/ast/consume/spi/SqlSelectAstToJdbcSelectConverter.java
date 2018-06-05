/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.consume.spi;

import org.hibernate.sql.ast.produce.spi.SqlAstSelectDescriptor;
import org.hibernate.sql.ast.tree.spi.SelectStatement;
import org.hibernate.sql.exec.internal.JdbcSelectImpl;
import org.hibernate.sql.exec.internal.StandardResultSetMappingDescriptor;
import org.hibernate.sql.exec.spi.JdbcSelect;
import org.hibernate.sql.exec.spi.ParameterBindingContext;
import org.hibernate.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;

import org.jboss.logging.Logger;

/**
 * The final phase of query translation.  Here we take the SQL-AST an
 * "interpretation".  For a select query, that means an instance of
 * {@link JdbcSelect}.
 *
 * @author Steve Ebersole
 */
public class SqlSelectAstToJdbcSelectConverter
		extends AbstractSqlAstToJdbcOperationConverter
		implements SqlSelectAstWalker, JdbcRecommendedSqlTypeMappingContext {
	private static final Logger log = Logger.getLogger( SqlSelectAstToJdbcSelectConverter.class );

	/**
	 * Perform interpretation of a select query, returning the SqlSelectInterpretation
	 *
	 * @return The interpretation result
	 */
	public static JdbcSelect interpret(
			SqlAstSelectDescriptor sqlSelectPlan,
			ParameterBindingContext parameterBindingContext) {
		final SqlSelectAstToJdbcSelectConverter walker = new SqlSelectAstToJdbcSelectConverter(
				parameterBindingContext
		);

		walker.visitSelectQuery( sqlSelectPlan.getSqlAstStatement() );
		return new JdbcSelectImpl(
				walker.getSql(),
				walker.getParameterBinders(),
				new StandardResultSetMappingDescriptor(
						sqlSelectPlan.getSqlAstStatement().getQuerySpec().getSelectClause().getSqlSelections(),
						sqlSelectPlan.getQueryResults()
				),
				sqlSelectPlan.getAffectedTableNames()
		);
	}

	private SqlSelectAstToJdbcSelectConverter(ParameterBindingContext parameterBindingContext) {
		super( parameterBindingContext );
	}

	@Override
	public void visitSelectQuery(SelectStatement selectQuery) {
		visitQuerySpec( selectQuery.getQuerySpec() );

	}
}
