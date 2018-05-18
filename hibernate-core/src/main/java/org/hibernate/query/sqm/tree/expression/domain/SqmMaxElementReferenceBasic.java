/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.metamodel.model.domain.spi.BasicCollectionElement;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;

/**
 * @author Steve Ebersole
 */
public class SqmMaxElementReferenceBasic
		extends AbstractSpecificSqmElementReference
		implements SqmRestrictedCollectionElementReferenceBasic, SqmMaxElementReference {
	public SqmMaxElementReferenceBasic(SqmPluralAttributeReference pluralAttributeBinding, SqmCreationContext creationContext) {
		super( pluralAttributeBinding, creationContext );
	}

	@Override
	public SqmMaxElementReferenceBasic copy(SqmCopyContext context) {
		return new SqmMaxElementReferenceBasic(
				getPluralAttributeReference().copy( context ),
				context.getCreationContext()
		);
	}

	@Override
	public BasicCollectionElement getExpressableType() {
		return (BasicCollectionElement) getReferencedNavigable();
	}

	@Override
	public BasicCollectionElement getInferableType() {
		return getExpressableType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMaxElementBinding( this );
	}

	@Override
	public String asLoggableText() {
		return "MAXELEMENT( " + getPluralAttributeReference().asLoggableText() + ")";
	}

	@Override
	public SqmPluralAttributeReference getSourceReference() {
		return  getPluralAttributeReference();
	}

	@Override
	public BasicCollectionElement getReferencedNavigable() {
		return (BasicCollectionElement) getPluralAttributeReference().getReferencedNavigable()
				.getPersistentCollectionDescriptor()
				.getElementDescriptor();
	}
}
