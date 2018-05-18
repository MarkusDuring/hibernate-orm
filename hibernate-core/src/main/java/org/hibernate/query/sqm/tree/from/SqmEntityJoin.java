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
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;

/**
 * @author Steve Ebersole
 */
public class SqmEntityJoin
		extends AbstractSqmJoin
		implements SqmQualifiedJoin {
	private final SqmEntityReference joinedEntityReference;
	private SqmPredicate onClausePredicate;

	public SqmEntityJoin(
			SqmFromElementSpace fromElementSpace,
			String uid,
			String alias,
			EntityDescriptor joinedEntityDescriptor,
			SqmJoinType joinType,
			SqmCreationContext creationContext) {
		super(
				fromElementSpace,
				uid,
				alias,
				joinType,
				creationContext
		);
		this.joinedEntityReference = new SqmEntityReference( joinedEntityDescriptor, this, creationContext );
	}

	@Override
	public SqmEntityReference getNavigableReference() {
		return joinedEntityReference;
	}

	public String getEntityName() {
		return getNavigableReference().getReferencedNavigable().getEntityName();
	}

	@Override
	public SqmPredicate getOnClausePredicate() {
		return onClausePredicate;
	}

	public void setOnClausePredicate(SqmPredicate predicate) {
		this.onClausePredicate = predicate;
	}

	@Override
	public SqmEntityJoin copy(SqmCopyContext context) {
		return context.copy(this, () -> new SqmEntityJoin(
				context.getCreationContext().getCurrentFromElementSpace(),
				getUniqueIdentifier(),
				getIdentificationVariable(),
				(EntityDescriptor) joinedEntityReference.getReferencedNavigable(),
				getSqmJoinType(),
				context.getCreationContext()
		));
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitQualifiedEntityJoinFromElement( this );
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getNavigableReference().getJavaTypeDescriptor();
	}

	@Override
	public Path<?> getParentPath() {
		// todo : shouldn't an entity join have a LHS as well?
		return null;
	}

	@Override
	public From getParent() {
		// todo : shouldn't an entity join have a LHS as well?
		return null;
	}

	@Override
	public Attribute getAttribute() {
		// There is no attribute for an entity join
		return null;
	}
}
