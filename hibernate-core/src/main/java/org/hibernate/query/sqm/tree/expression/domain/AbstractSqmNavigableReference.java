/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.AbstractSqmExpression;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmNavigableReference extends AbstractSqmExpression implements SqmNavigableReference {
	private final SqmCreationContext creationContext;
	public AbstractSqmNavigableReference(SqmCreationContext creationContext) {
		super( creationContext.getSessionFactory() );
		this.creationContext = creationContext;
	}

	@Override
	public abstract AbstractSqmNavigableReference copy(SqmCopyContext context);

	@Override
	public SqmCreationContext getCreationContext() {
		return creationContext;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + getNavigablePath().getFullPath() + ")";
	}

	protected final RuntimeException illegalDereference() {
		return new IllegalStateException("Illegal attempt to dereference path source [" + getNavigablePath().getFullPath() + "] of basic type");
	}
}
