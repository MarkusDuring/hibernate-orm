/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.internal;

import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.sql.exec.results.spi.CollectionReference;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractCollectionReference implements CollectionReference {
	private final PluralPersistentAttribute navigable;
	private final String resultVariable;

	protected AbstractCollectionReference(PluralPersistentAttribute navigable, String resultVariable) {
		this.navigable = navigable;
		this.resultVariable = resultVariable;
	}

	public PluralPersistentAttribute getNavigable() {
		return navigable;
	}

	public String getResultVariable() {
		return resultVariable;
	}

	@Override
	public PersistentCollectionDescriptor getCollectionMetadata() {
		return getNavigable().getPersistentCollectionMetadata();
	}
}
