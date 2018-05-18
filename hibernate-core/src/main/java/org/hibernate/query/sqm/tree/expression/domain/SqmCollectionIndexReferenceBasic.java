/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.metamodel.model.domain.spi.BasicCollectionIndex;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;

/**
 * @author Steve Ebersole
 */
public class SqmCollectionIndexReferenceBasic extends AbstractSqmCollectionIndexReference {
	public SqmCollectionIndexReferenceBasic(SqmPluralAttributeReference pluralAttributeBinding, SqmCreationContext creationContext) {
		super( pluralAttributeBinding, creationContext );
	}

	@Override
	public SqmCollectionIndexReferenceBasic copy(SqmCopyContext context) {
		return new SqmCollectionIndexReferenceBasic(
				getPluralAttributeReference().copy( context ),
				context.getCreationContext()
		);
	}

	@Override
	public BasicCollectionIndex getReferencedNavigable() {
		return (BasicCollectionIndex) getPluralAttributeReference().getReferencedNavigable()
				.getPersistentCollectionDescriptor()
				.getIndexDescriptor();
	}
}
