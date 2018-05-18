/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import org.hibernate.metamodel.model.domain.spi.EmbeddedValuedNavigable;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.expression.domain.SqmAttributeReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.sql.ast.produce.ConversionException;
import org.hibernate.sql.ast.produce.spi.FromClauseIndex;
import org.hibernate.sql.ast.tree.spi.from.TableGroup;
import org.hibernate.sql.ast.produce.metamodel.spi.Joinable;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import org.jboss.logging.Logger;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;

/**
 * Models a join based on a mapped attribute reference.
 *
 * @author Steve Ebersole
 */
public abstract class SqmNavigableJoin
		extends AbstractSqmJoin
		implements SqmQualifiedJoin, Fetch {
	private static final Logger log = Logger.getLogger( SqmNavigableJoin.class );

	private final SqmFrom lhs;
	private final SqmNavigableReference navigableReference;
	private final boolean fetched;

	private SqmPredicate onClausePredicate;

	public SqmNavigableJoin(
			SqmFrom lhs,
			SqmNavigableReference navigableReference,
			String uid,
			String alias,
			SqmJoinType joinType,
			boolean fetched,
			SqmCreationContext creationContext) {
		super(
				navigableReference.getSourceReference().getExportedFromElement().getContainingSpace(),
				uid,
				alias,
				joinType,
				creationContext
		);
		this.lhs = lhs;

		this.navigableReference = navigableReference;
		this.fetched = fetched;
	}



	public SqmFrom getLhs() {
		return lhs;
	}

	public SqmAttributeReference getAttributeReference() {
		return (SqmAttributeReference) navigableReference;
	}

	@Override
	public SqmNavigableReference getNavigableReference() {
		return getAttributeReference();
	}

	public boolean isFetched() {
		return fetched;
	}

	@Override
	public SqmPredicate getOnClausePredicate() {
		return onClausePredicate;
	}

	public void setOnClausePredicate(SqmPredicate predicate) {
		log.tracef(
				"Setting join predicate [%s] (was [%s])",
				predicate.toString(),
				this.onClausePredicate == null ? "<null>" : this.onClausePredicate.toString()
		);

		this.onClausePredicate = predicate;
	}

	@Override
	public SqmNavigableJoin copy(SqmCopyContext context) {
		// We copy/lookup the navigable reference first, because it will copy the join as well
		SqmNavigableReference copiedNavigableReference = context.copy(
				navigableReference,
				() -> navigableReference.copy(context)
		);
		return context.copy(
				this,
				() -> {
					Joinable<?> joinable = (Joinable<?>) getNavigableReference().getReferencedNavigable();
					return (SqmNavigableJoin) joinable.createJoin(
							context.copy(
									lhs,
									() -> lhs.copy( context )
							),
							copiedNavigableReference,
							getUniqueIdentifier(),
							getIdentificationVariable(),
							getSqmJoinType(),
							fetched,
							context.getCreationContext()
					);
				}
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitQualifiedAttributeJoinFromElement( this );
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return navigableReference.getJavaTypeDescriptor();
	}

	@Override
	public TableGroup locateMapping(FromClauseIndex fromClauseIndex) {
		if ( getNavigableReference().getReferencedNavigable() instanceof EmbeddedValuedNavigable ) {
			return fromClauseIndex.findResolvedTableGroup( getLhs() );
		}

		try {
			return fromClauseIndex.resolveTableGroup( getUniqueIdentifier() );
		}
		catch (ConversionException e) {
			// our uid is not yet known.. we should create the TableGroup here - at least initiate it
			return null;
		}
	}

	@Override
	public Path<?> getParentPath() {
		return lhs;
	}

	@Override
	public Join on(Expression restriction) {
		setOnClausePredicate( (SqmPredicate) getCriteriaBuilder().wrap( restriction ) );
		return this;
	}

	@Override
	public Join on(Predicate... restrictions) {
		setOnClausePredicate( (SqmPredicate) getCriteriaBuilder().and( restrictions ) );
		return this;
	}

	@Override
	public Predicate getOn() {
		return onClausePredicate;
	}

	@Override
	public Attribute getAttribute() {
		return (Attribute) navigableReference.getReferencedNavigable();
	}

	@Override
	public From getParent() {
		return lhs;
	}

	@Override
	public JoinType getJoinType() {
		return getSqmJoinType().getCorrespondingJpaJoinType();
	}
}
