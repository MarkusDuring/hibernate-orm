/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.metamodel.model.domain.spi.PersistentAttribute;
import org.hibernate.query.sqm.tree.from.SqmFromExporter;

import javax.persistence.criteria.Path;

/**
 * Models the binding of a persistent attribute of the domain model.
 *
 * @author Steve Ebersole
 */
public interface SqmAttributeReference extends SqmNavigableReference, Path {
	@Override
	PersistentAttribute getReferencedNavigable();
}
