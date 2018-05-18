/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.metamodel.model.domain.spi.CollectionElement;
import org.hibernate.metamodel.model.domain.spi.CollectionElementEmbedded;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class SqmMinElementReferenceEmbedded
		extends AbstractSpecificSqmElementReference
		implements SqmMinElementReference, SqmEmbeddableTypedReference {

	private SqmFrom exportedFromElement;

	public SqmMinElementReferenceEmbedded(SqmPluralAttributeReference pluralAttributeBinding, SqmCreationContext creationContext) {
		super( pluralAttributeBinding, creationContext );
	}

	@Override
	public SqmMinElementReferenceEmbedded copy(SqmCopyContext context) {
		return new SqmMinElementReferenceEmbedded(
				getPluralAttributeReference().copy( context ),
				context.getCreationContext()
		);
	}

	@Override
	public CollectionElement getExpressableType() {
		return getPluralAttributeReference().getReferencedNavigable().getPersistentCollectionDescriptor().getElementDescriptor();
	}

	@Override
	public CollectionElement getInferableType() {
		return getExpressableType();
	}

	@Override
	public CollectionElementEmbedded getReferencedNavigable() {
		return (CollectionElementEmbedded) super.getReferencedNavigable();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return null;
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return exportedFromElement;
	}
}
