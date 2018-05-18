/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;

import java.util.function.Supplier;

/**
 * A context for defining copy options.
 *
 * @author Christian Beikov
 */
public interface SqmCopyContext {

    <T> T copy(T original, Supplier<T> copier);

    <T> T getCopy(T original);

    SqmCreationContext getCreationContext();

}
