/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import org.hibernate.metamodel.model.domain.spi.PersistentAttribute;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionIndexReferenceBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmMapEntryBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 */
public class SqmMapNavigableJoin extends SqmPluralNavigableJoin implements MapJoin {

	public SqmMapNavigableJoin(
			SqmFrom lhs,
			SqmNavigableReference navigableReference,
			String uid,
			String alias,
			SqmJoinType joinType,
			boolean fetched,
			SqmCreationContext creationContext) {
		super(
				lhs,
				navigableReference,
				uid,
				alias,
				joinType,
				fetched,
				creationContext
		);
	}

	@Override
	public MapAttribute getModel() {
		return (MapAttribute) super.getModel();
	}

	@Override
	public MapJoin on(Expression restriction) {
		return (MapJoin) super.on(restriction);
	}

	@Override
	public MapJoin on(Predicate... restrictions) {
		return (MapJoin) super.on(restrictions);
	}

	@Override
	public Path key() {
		return (Path) getNavigableReference().getReferencedNavigable()
				.getPersistentCollectionDescriptor()
				.getIndexDescriptor()
				.createSqmExpression( this, getNavigableReference(), getCreationContext() );
	}

	@Override
	public Path value() {
		return (Path) getNavigableReference().getReferencedNavigable()
				.getPersistentCollectionDescriptor()
				.getElementDescriptor()
				.createSqmExpression( this, getNavigableReference(), getCreationContext() );
	}

	@Override
	public Expression<Map.Entry> entry() {
		return new SqmMapEntryBinding(
				getNavigableReference(),
				(BasicJavaDescriptor) getSessionFactory().getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( Map.Entry.class ),
				getSessionFactory()
		);
	}
}
