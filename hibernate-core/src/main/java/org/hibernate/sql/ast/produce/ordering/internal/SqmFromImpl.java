/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.ordering.internal;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.metamodel.model.domain.spi.NavigableContainer;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.sqm.ParsingException;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.produce.path.spi.SemanticPathPart;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.AbstractSqmExpression;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableContainerReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmRestrictedCollectionElementReference;
import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.query.sqm.tree.from.SqmFromClause;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.UsageDetails;
import org.hibernate.query.sqm.tree.from.UsageDetailsImpl;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.sql.ast.produce.metamodel.spi.NavigableContainerReferenceInfo;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.*;

/**
 * @author Steve Ebersole
 */
public class SqmFromImpl implements SqmFrom {
	private final PersistentCollectionDescriptor collectionDescriptor;
	private final SqmFromElementSpace space;
	private final SqmNavigableReference navRef;
	private final String uid;
	private final String alias;

	private final UsageDetailsImpl usageDetails = new UsageDetailsImpl( this );

	protected SqmFromImpl(SqmCreationContext creationContext, PersistentCollectionDescriptor collectionDescriptor) {
		this.collectionDescriptor = collectionDescriptor;
		this.space = createFromElementSpace();
		this.uid = generateUid();
		this.alias = creationContext.getImplicitAliasGenerator().generateUniqueImplicitAlias();
		this.navRef = new SqmNavigableReferenceImpl( creationContext );
	}

	private static SqmFromElementSpace createFromElementSpace() {
		SqmFromClause fromClause = new SqmFromClause();
		return fromClause.makeFromElementSpace();
	}

	private static String generateUid() {
		return "<gen:orderByFragmentParsing>";
	}

    @Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return collectionDescriptor.getJavaTypeDescriptor();
	}

	@Override
	public SqmFromElementSpace getContainingSpace() {
		return space;
	}

	@Override
	public String getUniqueIdentifier() {
		return uid;
	}

	@Override
	public SqmNavigableReference getNavigableReference() {
		return navRef;
	}

	@Override
	public UsageDetails getUsageDetails() {
		return usageDetails;
	}

	@Override
	public String getIdentificationVariable() {
		return alias;
	}

	@Override
	public EntityDescriptor getIntrinsicSubclassEntityMetadata() {
		return (EntityDescriptor) getUsageDetails().getIntrinsicSubclassIndicator();
	}

	@Override
	public SqmFrom copy(SqmCopyContext context) {
		return context.copy(this, () -> new SqmFromImpl(
				context.getCreationContext(),
				collectionDescriptor
		));
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		throw new ParsingException(
				"OrderByFragmentParser-generated SqmFrom should not be visited"
		);
	}

	private class SqmNavigableReferenceImpl extends AbstractSqmExpression implements SqmNavigableContainerReference, Path {
		private final SqmCreationContext creationContext;
		private final NavigablePath navigablePath = new NavigablePath( collectionDescriptor.getNavigableRole().getFullPath() );

		public SqmNavigableReferenceImpl( SqmCreationContext creationContext ) {
			super( creationContext.getSessionFactory() );
			this.creationContext = creationContext;
		}

		@Override
		public NavigableContainerReferenceInfo getNavigableContainerReferenceInfo() {
			return null;
		}

		@Override
		public String getUniqueIdentifier() {
			return uid;
		}

		@Override
		public String getIdentificationVariable() {
			return alias;
		}

		@Override
		public EntityDescriptor getIntrinsicSubclassEntityMetadata() {
			return null;
		}

		@Override
		public ExpressableType getExpressableType() {
			return null;
		}

		@Override
		public ExpressableType getInferableType() {
			return null;
		}

		@Override
		public SqmNavigableReferenceImpl copy(SqmCopyContext context) {
			return this;
		}

		@Override
		public <T> T accept(SemanticQueryWalker<T> walker) {
			throw new ParsingException(
					"OrderByFragmentParser-generated SqmNavigableReference should not be visited"
			);
		}

		@Override
		public String asLoggableText() {
			return String.format(
					Locale.ROOT,
					"{%s -> %s(%s)}",
					OrderByFragmentParser.class.getSimpleName(),
					SqmFromImpl.class.getSimpleName(),
					collectionDescriptor.getNavigableRole().getFullPath()
			);
		}

		@Override
		public JavaTypeDescriptor getJavaTypeDescriptor() {
			return null;
		}

		@Override
		public SqmNavigableContainerReference getSourceReference() {
			return null;
		}

		@Override
		public NavigableContainer getReferencedNavigable() {
			return collectionDescriptor;
		}

		@Override
		public NavigablePath getNavigablePath() {
			return navigablePath;
		}

		@Override
		public PersistenceType getPersistenceType() {
			return collectionDescriptor.getElementDescriptor().getPersistenceType();
		}

		@Override
		public Class getJavaType() {
			return collectionDescriptor.getJavaType();
		}


		@Override
		public SemanticPathPart resolvePathPart(
				String name,
				String currentContextKey,
				boolean isTerminal,
				SqmCreationContext context) {
			final Navigable navigable = collectionDescriptor.findNavigable( name );
			return navigable.createSqmExpression( SqmFromImpl.this, this, context );
		}

		@Override
		public SqmRestrictedCollectionElementReference resolveIndexedAccess(
				SqmExpression selector,
				String currentContextKey,
				boolean isTerminal,
				SqmCreationContext context) {
			throw new UnsupportedOperationException(  );
		}

		@Override
		public SqmFrom getExportedFromElement() {
			return SqmFromImpl.this;
		}

		@Override
		public SqmCreationContext getCreationContext() {
			return creationContext;
		}

		// The following methods aren't implemented because they are not needed
		// SqmFrom implement the Criteria API but the object itself never "escapes" from the sort expression
		// so there is no need to implement this properly here

		@Override
		public Bindable getModel() {
			return null;
		}

		@Override
		public Path<?> getParentPath() {
			return null;
		}

		@Override
		public Path get(SingularAttribute attribute) {
			return null;
		}

		@Override
		public Expression get(PluralAttribute collection) {
			return null;
		}

		@Override
		public Expression get(MapAttribute map) {
			return null;
		}

		@Override
		public Expression<Class> type() {
			return null;
		}

		@Override
		public Path get(String attributeName) {
			return null;
		}
	}

	@Override
	public Set<Join> getJoins() {
		return null;
	}

	@Override
	public boolean isCorrelated() {
		return false;
	}

	@Override
	public From getCorrelationParent() {
		return null;
	}

	@Override
	public Join join(SingularAttribute attribute) {
		return null;
	}

	@Override
	public Join join(SingularAttribute attribute, JoinType jt) {
		return null;
	}

	@Override
	public CollectionJoin join(CollectionAttribute collection) {
		return null;
	}

	@Override
	public SetJoin join(SetAttribute set) {
		return null;
	}

	@Override
	public ListJoin join(ListAttribute list) {
		return null;
	}

	@Override
	public MapJoin join(MapAttribute map) {
		return null;
	}

	@Override
	public CollectionJoin join(CollectionAttribute collection, JoinType jt) {
		return null;
	}

	@Override
	public SetJoin join(SetAttribute set, JoinType jt) {
		return null;
	}

	@Override
	public ListJoin join(ListAttribute list, JoinType jt) {
		return null;
	}

	@Override
	public MapJoin join(MapAttribute map, JoinType jt) {
		return null;
	}

	@Override
	public Join join(String attributeName) {
		return null;
	}

	@Override
	public CollectionJoin joinCollection(String attributeName) {
		return null;
	}

	@Override
	public SetJoin joinSet(String attributeName) {
		return null;
	}

	@Override
	public ListJoin joinList(String attributeName) {
		return null;
	}

	@Override
	public MapJoin joinMap(String attributeName) {
		return null;
	}

	@Override
	public Join join(String attributeName, JoinType jt) {
		return null;
	}

	@Override
	public CollectionJoin joinCollection(String attributeName, JoinType jt) {
		return null;
	}

	@Override
	public SetJoin joinSet(String attributeName, JoinType jt) {
		return null;
	}

	@Override
	public ListJoin joinList(String attributeName, JoinType jt) {
		return null;
	}

	@Override
	public MapJoin joinMap(String attributeName, JoinType jt) {
		return null;
	}

	@Override
	public Predicate isNotNull() {
		return null;
	}

	@Override
	public Predicate in(Collection values) {
		return null;
	}

	@Override
	public Set<Fetch> getFetches() {
		return null;
	}

	@Override
	public Fetch fetch(SingularAttribute attribute) {
		return null;
	}

	@Override
	public Fetch fetch(SingularAttribute attribute, JoinType jt) {
		return null;
	}

	@Override
	public Fetch fetch(PluralAttribute attribute) {
		return null;
	}

	@Override
	public Fetch fetch(PluralAttribute attribute, JoinType jt) {
		return null;
	}

	@Override
	public Fetch fetch(String attributeName) {
		return null;
	}

	@Override
	public Fetch fetch(String attributeName, JoinType jt) {
		return null;
	}

	@Override
	public Bindable getModel() {
		return null;
	}

	@Override
	public Path<?> getParentPath() {
		return null;
	}

	@Override
	public Path get(SingularAttribute attribute) {
		return null;
	}

	@Override
	public Expression get(PluralAttribute collection) {
		return null;
	}

	@Override
	public Expression get(MapAttribute map) {
		return null;
	}

	@Override
	public Expression<Class> type() {
		return null;
	}

	@Override
	public Path get(String attributeName) {
		return null;
	}

	@Override
	public Predicate isNull() {
		return null;
	}

	@Override
	public Predicate in(Object... values) {
		return null;
	}

	@Override
	public Predicate in(Expression[] values) {
		return null;
	}

	@Override
	public Predicate in(Expression values) {
		return null;
	}

	@Override
	public Expression as(Class type) {
		return null;
	}

	@Override
	public Selection alias(String name) {
		return null;
	}

	@Override
	public boolean isCompoundSelection() {
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		return null;
	}

	@Override
	public Class getJavaType() {
		return null;
	}

	@Override
	public String getAlias() {
		return null;
	}
}
