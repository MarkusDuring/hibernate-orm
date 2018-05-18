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
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityReference;
import org.hibernate.sql.ast.produce.metamodel.spi.EntityValuedExpressableType;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

/**
 * @author Steve Ebersole
 */
public class SqmRoot extends AbstractSqmFrom implements Root {
	private final SqmEntityReference entityReference;

	public SqmRoot(
			SqmFromElementSpace fromElementSpace,
			String uid,
			String alias,
			EntityValuedExpressableType entityReference,
			SqmCreationContext creationContext) {
		super( fromElementSpace, uid, alias, creationContext );
		this.entityReference = new SqmEntityReference( entityReference.getEntityDescriptor(), this, creationContext );
	}

	private SqmRoot(
			SqmFromElementSpace fromElementSpace,
			String uid,
			String alias,
			EntityDescriptor entityDescriptor,
			SqmCreationContext creationContext) {
		super(fromElementSpace, uid, alias, creationContext);
		this.entityReference = new SqmEntityReference( entityDescriptor, this, creationContext );
	}

	@Override
	public SqmEntityReference getNavigableReference() {
		return entityReference;
	}

	public String getEntityName() {
		return getNavigableReference().getReferencedNavigable().getEntityName();
	}

	@Override
	public EntityDescriptor getIntrinsicSubclassEntityMetadata() {
		// a root FromElement cannot indicate a subclass intrinsically (as part of its declaration)
		return null;
	}

	@Override
	public String toString() {
		return getEntityName() + " as " + getIdentificationVariable();
	}

	@Override
	public SqmRoot copy(SqmCopyContext context) {
		return context.copy(this, () -> new SqmRoot(
				context.getCreationContext().getCurrentFromElementSpace(),
				getUniqueIdentifier(),
				getIdentificationVariable(),
				(EntityDescriptor) entityReference.getReferencedNavigable(),
				context.getCreationContext()
		));
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitRootEntityFromElement( this );
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getNavigableReference().getJavaTypeDescriptor();
	}

	@Override
	public EntityType getModel() {
		return (EntityType) super.getModel();
	}

	@Override
	public Path<?> getParentPath() {
		return null;
	}
}
