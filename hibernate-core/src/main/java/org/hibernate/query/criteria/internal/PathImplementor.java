/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal;

import org.hibernate.engine.spi.SessionFactoryImplementor;

import javax.persistence.criteria.Path;

/**
 * Hibernate extensions to the JPA Path.
 *
 * @author Christian Beikov
 */
public interface PathImplementor<X> extends Path<X> {

    SessionFactoryImplementor getSessionFactory();

}
