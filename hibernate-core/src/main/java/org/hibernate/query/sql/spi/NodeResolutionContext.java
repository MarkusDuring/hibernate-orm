/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.spi;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.ast.tree.spi.select.SqlSelectionResolver;

/**
 * @author Steve Ebersole
 */
public interface NodeResolutionContext {
	SessionFactoryImplementor getSessionFactory();
	SqlSelectionResolver getSqlSelectionResolver();
}
