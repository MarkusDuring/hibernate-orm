/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.queryable.spi;

import org.hibernate.metamodel.model.domain.spi.NavigableContainer;
import org.hibernate.metamodel.model.domain.spi.NavigableVisitationStrategy;
import org.hibernate.sql.ast.produce.spi.SqlSelectPlan;

/**
 * Specialization {@link NavigableVisitationStrategy} implementation for
 * building {@link SqlSelectPlan} instances driven primarily by mapping
 * metadata.
 *
 * @author Steve Ebersole
 */
public interface MetamodelDrivenSqlSelectPlanBuilder {
	/**
	 * Build the SqlSelectPlan, driven by mapping model, with the given
	 * NavigableSource as query root..  The mapping model indicates the shape
	 * of the SelectQuery AST ({@link SqlSelectPlan#getSqlAstSelectStatement}) as well as the shape of the results
	 * as indicated by the query Return graphs ({@link SqlSelectPlan#getQueryResults}).
	 *
	 * @param rootNavigable The NavigableSource which is the root of the query.
	 */
	SqlSelectPlan buildSqlSelectPlan(NavigableContainer rootNavigable);
}
