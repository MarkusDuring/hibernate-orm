/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class SqmIndexedElementReferenceBasic
		extends AbstractSqmIndexedElementReference
		implements SqmRestrictedCollectionElementReferenceBasic {
	public SqmIndexedElementReferenceBasic(
			SqmPluralAttributeReference pluralAttributeBinding,
			SqmExpression indexSelectionExpression,
			SqmCreationContext creationContext) {
		super( pluralAttributeBinding, indexSelectionExpression, creationContext );
	}

	@Override
	public SqmIndexedElementReferenceBasic copy(SqmCopyContext context) {
		return new SqmIndexedElementReferenceBasic(
				getPluralAttributeReference().copy( context ),
				getIndexSelectionExpression().copy( context ),
				context.getCreationContext()
		);
	}
}
