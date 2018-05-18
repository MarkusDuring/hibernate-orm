/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.metamodel.model.domain.spi.CollectionElementEmbedded;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class SqmIndexedElementReferenceEmbedded
		extends AbstractSqmIndexedElementReference
		implements SqmRestrictedCollectionElementReferenceEmbedded {
	public SqmIndexedElementReferenceEmbedded(
			SqmPluralAttributeReference pluralAttributeBinding,
			SqmExpression indexSelectionExpression,
			SqmCreationContext creationContext) {
		super( pluralAttributeBinding, indexSelectionExpression, creationContext );
	}

	@Override
	public SqmIndexedElementReferenceEmbedded copy(SqmCopyContext context) {
		return new SqmIndexedElementReferenceEmbedded(
				getPluralAttributeReference().copy( context ),
				getIndexSelectionExpression().copy( context ),
				context.getCreationContext()
		);
	}

	@Override
	public CollectionElementEmbedded getReferencedNavigable() {
		return (CollectionElementEmbedded) getPluralAttributeReference().getReferencedNavigable().getPersistentCollectionDescriptor().getElementDescriptor();
	}

	@Override
	public CollectionElementEmbedded getExpressableType() {
		return getReferencedNavigable();
	}

	@Override
	public CollectionElementEmbedded getInferableType() {
		return getExpressableType();
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return getPluralAttributeReference().getExportedFromElement();
	}

}
