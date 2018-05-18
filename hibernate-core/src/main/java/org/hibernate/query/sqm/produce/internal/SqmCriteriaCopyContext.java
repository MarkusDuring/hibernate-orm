/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.produce.internal;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 * @author Christian Beikov
 */
public class SqmCriteriaCopyContext implements SqmCopyContext {

	private final SqmCreationContext creationContext;
	private final Map<Object, Object> copyMap = new IdentityHashMap<>();

	public SqmCriteriaCopyContext(SqmCreationContext creationContext) {
		this.creationContext = creationContext;
	}

	@Override
	public <T> T copy(T original, Supplier<T> copier) {
		return ((Map<T, T>) copyMap).computeIfAbsent(original, o -> copier.get() );
	}

	@Override
	public <T> T getCopy(T original) {
		return (T) copyMap.get( original );
	}

	@Override
	public SqmCreationContext getCreationContext() {
		return creationContext;
	}
}
