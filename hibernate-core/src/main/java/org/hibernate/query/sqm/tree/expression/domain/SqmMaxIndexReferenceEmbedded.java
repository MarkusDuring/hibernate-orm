/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.metamodel.model.domain.spi.EmbeddedValuedNavigable;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class SqmMaxIndexReferenceEmbedded
		extends AbstractSpecificSqmCollectionIndexReference
		implements SqmMaxIndexReference, SqmEmbeddableTypedReference {
	private SqmFrom exportedFromElement;

	public SqmMaxIndexReferenceEmbedded(SqmPluralAttributeReference pluralAttributeBinding, SqmCreationContext creationContext) {
		super( pluralAttributeBinding, creationContext );
	}

	@Override
	public SqmMaxIndexReferenceEmbedded copy(SqmCopyContext context) {
		return new SqmMaxIndexReferenceEmbedded(
				getPluralAttributeReference().copy( context ),
				context.getCreationContext()
		);
	}

	@Override
	public EmbeddedValuedNavigable getReferencedNavigable() {
		return (EmbeddedValuedNavigable) super.getReferencedNavigable();
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return exportedFromElement;
	}
}
