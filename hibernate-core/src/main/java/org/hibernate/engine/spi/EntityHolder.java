/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.engine.spi;

import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.results.graph.entity.EntityInitializer;
import org.hibernate.sql.results.jdbc.spi.JdbcValuesSourceProcessingState;

public interface EntityHolder {
	EntityKey getEntityKey();
	EntityPersister getDescriptor();
	Object getEntity();
	Object getProxy();
	EntityInitializer getEntityInitializer();

	default Object getManagedObject() {
		final Object proxy = getProxy();
		return proxy == null ? getEntity() : proxy;
	}

	void markAsReloaded(JdbcValuesSourceProcessingState processingState);

	boolean isEventuallyInitialized();
}
