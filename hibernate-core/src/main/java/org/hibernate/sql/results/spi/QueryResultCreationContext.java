/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.spi;

import org.hibernate.LockOptions;

/**
 * Contextual information useful when creating a QueryResult.
 *
 * @see QueryResultProducer#createQueryResult
 *
 * @author Steve Ebersole
 */
public interface QueryResultCreationContext extends SqlSelectionResolutionContext {
	LockOptions getLockOptions();
}
