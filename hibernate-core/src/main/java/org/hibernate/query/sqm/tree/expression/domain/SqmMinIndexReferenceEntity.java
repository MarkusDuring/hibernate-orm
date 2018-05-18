/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.sql.ast.produce.metamodel.spi.EntityValuedExpressableType;

/**
 * @author Steve Ebersole
 */
public class SqmMinIndexReferenceEntity
		extends AbstractSpecificSqmCollectionIndexReference
		implements SqmMinIndexReference, SqmEntityTypedReference {
	private SqmFrom exportedFromElement;

	public SqmMinIndexReferenceEntity(SqmPluralAttributeReference pluralAttributeBinding, SqmCreationContext creationContext) {
		super( pluralAttributeBinding, creationContext );
	}

	@Override
	public SqmMinIndexReferenceEntity copy(SqmCopyContext context) {
		return new SqmMinIndexReferenceEntity(
				getPluralAttributeReference().copy( context ),
				context.getCreationContext()
		);
	}

	@Override
	public EntityValuedExpressableType getReferencedNavigable() {
		return (EntityValuedExpressableType) super.getReferencedNavigable();
	}

	@Override
	public EntityValuedExpressableType getExpressableType() {
		return getReferencedNavigable();
	}

	@Override
	public EntityValuedExpressableType getInferableType() {
		return getReferencedNavigable();
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return exportedFromElement;
	}
}
