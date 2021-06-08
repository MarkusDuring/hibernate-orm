/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.sql.exec.spi;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.ast.spi.AfterLoadAction;
import org.hibernate.persister.entity.Loadable;

/**
 * Callback to allow SQM interpretation to trigger certain things within ORM.  See the current
 * {@link AfterLoadAction} javadocs for details.  Specifically this would
 * encompass things like follow-on locking, follow-on fetching, etc.
 *
 * @author Steve Ebersole
 */
public interface Callback {
	void registerAfterLoadAction(AfterLoadAction afterLoadAction);

	void invokeAfterLoadActions(SharedSessionContractImplementor session, Object entity, Loadable persister);
}
