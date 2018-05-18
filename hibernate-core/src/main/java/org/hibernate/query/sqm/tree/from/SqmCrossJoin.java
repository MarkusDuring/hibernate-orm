/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityReference;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.Path;

/**
 * @author Steve Ebersole
 */
public class SqmCrossJoin extends AbstractSqmFrom implements SqmJoin {
	private final SqmEntityReference joinedEntityReference;

	public SqmCrossJoin(
			SqmFromElementSpace fromElementSpace,
			String uid,
			String alias,
			EntityDescriptor entityDescriptor,
			SqmCreationContext creationContext) {
		super(
				fromElementSpace,
				uid,
				alias,
				creationContext
        );
		this.joinedEntityReference = new SqmEntityReference( entityDescriptor, this, creationContext );
	}

	@Override
	public SqmEntityReference getNavigableReference() {
		return joinedEntityReference;
	}

	public String getEntityName() {
		return getNavigableReference().getReferencedNavigable().getEntityName();
	}

	@Override
	public SqmJoinType getSqmJoinType() {
		return SqmJoinType.CROSS;
	}

	@Override
	public SqmCrossJoin copy(SqmCopyContext context) {
		return context.copy(this, () -> new SqmCrossJoin(
				context.getCreationContext().getCurrentFromElementSpace(),
				getUniqueIdentifier(),
				getIdentificationVariable(),
				(EntityDescriptor) joinedEntityReference.getReferencedNavigable(),
				context.getCreationContext()
		));
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCrossJoinedFromElement( this );
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getNavigableReference().getJavaTypeDescriptor();
	}

	@Override
	public Path<?> getParentPath() {
		return null;
	}
}
