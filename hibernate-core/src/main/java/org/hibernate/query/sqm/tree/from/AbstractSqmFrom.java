/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.query.sqm.produce.internal.SqmFromBuilderFromClauseQualifiedJoin;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.produce.spi.SqmFromBuilder;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.expression.AbstractSqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityTypeExpression;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Convenience base class for FromElement implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractSqmFrom extends AbstractSqmExpression implements SqmFrom {
	private final SqmCreationContext creationContext;
	private final SqmFromElementSpace fromElementSpace;
	private final String uid;
	private final String alias;

	private final UsageDetailsImpl usageDetails = new UsageDetailsImpl( this );

	protected AbstractSqmFrom(
			SqmFromElementSpace fromElementSpace,
			String uid,
			String alias,
			SqmCreationContext creationContext) {
		super( creationContext.getSessionFactory() );
		this.creationContext = creationContext;
		this.fromElementSpace = fromElementSpace;
		this.uid = uid;
		this.alias = alias;
	}

	@Override
	public abstract AbstractSqmFrom copy(SqmCopyContext context);

	protected final SqmCreationContext getCreationContext() {
		return creationContext;
	}

	@Override
	public SqmFromElementSpace getContainingSpace() {
		return fromElementSpace;
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
	public UsageDetails getUsageDetails() {
		return usageDetails;
	}

	@Override
	public EntityDescriptor getIntrinsicSubclassEntityMetadata() {
		return (EntityDescriptor) getUsageDetails().getIntrinsicSubclassIndicator();
	}

	@Override
	public Set<Join> getJoins() {
		List<SqmJoin> sqmJoins = fromElementSpace.getJoins();
		Set<Join> joins = new HashSet<>( sqmJoins.size() );
		for ( SqmJoin sqmJoin : sqmJoins ) {
			if ( sqmJoin instanceof SqmNavigableJoin ) {
				SqmNavigableJoin navigableJoin = (SqmNavigableJoin) sqmJoin;
				if ( navigableJoin.getLhs() == this && !navigableJoin.isFetched() ) {
					sqmJoins.add( navigableJoin );
				}
			}
		}

		return joins;
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
		return join( attribute, JoinType.INNER );
	}

	@Override
	public CollectionJoin join(CollectionAttribute collection) {
		return join( collection, JoinType.INNER );
	}

	@Override
	public SetJoin join(SetAttribute set) {
		return join( set, JoinType.INNER );
	}

	@Override
	public ListJoin join(ListAttribute list) {
		return join( list, JoinType.INNER );
	}

	@Override
	public MapJoin join(MapAttribute map) {
		return join( map, JoinType.INNER );
	}

	@Override
	public Join join(SingularAttribute attribute, JoinType jt) {
		return join( attribute.getName(), jt );
	}

	@Override
	public CollectionJoin join(CollectionAttribute collection, JoinType jt) {
		return (CollectionJoin) join( collection.getName(), jt );
	}

	@Override
	public SetJoin join(SetAttribute set, JoinType jt) {
		return (SetJoin) join( set.getName(), jt );
	}

	@Override
	public ListJoin join(ListAttribute list, JoinType jt) {
		return (ListJoin) join( list.getName(), jt );
	}

	@Override
	public MapJoin join(MapAttribute map, JoinType jt) {
		return (MapJoin) join( map.getName(), jt );
	}

	@Override
	public Join join(String attributeName) {
		return join( attributeName, JoinType.INNER );
	}

	@Override
	public CollectionJoin joinCollection(String attributeName) {
		return joinCollection( attributeName, JoinType.INNER );
	}

	@Override
	public SetJoin joinSet(String attributeName) {
		return joinSet( attributeName, JoinType.INNER );
	}

	@Override
	public ListJoin joinList(String attributeName) {
		return joinList( attributeName, JoinType.INNER );
	}

	@Override
	public MapJoin joinMap(String attributeName) {
		return joinMap( attributeName, JoinType.INNER );
	}

	@Override
	public CollectionJoin joinCollection(String attributeName, JoinType jt) {
		return (CollectionJoin) join( attributeName, jt );
	}

	@Override
	public SetJoin joinSet(String attributeName, JoinType jt) {
		return (SetJoin) join( attributeName, jt );
	}

	@Override
	public ListJoin joinList(String attributeName, JoinType jt) {
		return (ListJoin) join( attributeName, jt );
	}

	@Override
	public MapJoin joinMap(String attributeName, JoinType jt) {
		return (MapJoin) join( attributeName, jt );
	}

	@Override
	public Join join(String attributeName, JoinType jt) {
		return (Join) get( attributeName, jt, false );
	}

	private Expression<?> get(String attributeName, JoinType jt, boolean fetched) {
		final SqmFromBuilder fromBuilder = new SqmFromBuilderFromClauseQualifiedJoin(
				getSqmJoinType( jt ),
				fetched,
				creationContext.getImplicitAliasGenerator().generateUniqueImplicitAlias(),
				creationContext
		);

		final SqmFromBuilder oldFromBuilder = creationContext.getCurrentFromElementBuilder();
		final SqmFromElementSpace oldSpace = creationContext.getCurrentFromElementSpace();
		try {
			creationContext.setCurrentFromElementBuilder( fromBuilder );
			creationContext.getCurrentSqmFromElementSpaceCoordAccess().setCurrentSqmFromElementSpace( getContainingSpace() );
			final SqmFromExporter fromExporter = (SqmFromExporter) getNavigableReference().resolvePathPart(
					attributeName,
					null,
					false,
					creationContext
			);
			SqmFrom exportedFromElement = fromExporter.getExportedFromElement();
			if ( exportedFromElement instanceof SqmNavigableJoin ) {
				return (SqmNavigableJoin) exportedFromElement;
			}
			return (Expression<?>) fromExporter;
		} finally {
			creationContext.setCurrentFromElementBuilder( oldFromBuilder );
			creationContext.getCurrentSqmFromElementSpaceCoordAccess().setCurrentSqmFromElementSpace( oldSpace );
		}
	}

	private static SqmJoinType getSqmJoinType(JoinType jt) {
		switch (jt) {
			case LEFT: return SqmJoinType.LEFT;
			case INNER: return SqmJoinType.INNER;
			case RIGHT: return SqmJoinType.RIGHT;
		}

		throw new UnsupportedOperationException( "Unsupported join type : " + jt );
	}

	@Override
	public Set<Fetch> getFetches() {
		List<SqmJoin> sqmJoins = fromElementSpace.getJoins();
		Set<Fetch> fetches = new HashSet<>( sqmJoins.size() );
		for ( SqmJoin sqmJoin : sqmJoins ) {
			if ( sqmJoin instanceof SqmNavigableJoin ) {
				SqmNavigableJoin navigableJoin = (SqmNavigableJoin) sqmJoin;
				if ( navigableJoin.getLhs() == this && navigableJoin.isFetched() ) {
					sqmJoins.add( navigableJoin );
				}
			}
		}

		return fetches;
	}

	@Override
	public Fetch fetch(SingularAttribute attribute) {
		return fetch( attribute, JoinType.INNER );
	}

	@Override
	public Fetch fetch(SingularAttribute attribute, JoinType jt) {
		return fetch( attribute.getName(), jt );
	}

	@Override
	public Fetch fetch(PluralAttribute attribute) {
		return fetch( attribute, JoinType.INNER );
	}

	@Override
	public Fetch fetch(PluralAttribute attribute, JoinType jt) {
		return fetch( attribute.getName(), jt );
	}

	@Override
	public Fetch fetch(String attributeName) {
		return fetch( attributeName, JoinType.INNER );
	}

	@Override
	public Fetch fetch(String attributeName, JoinType jt) {
		return (Fetch) get( attributeName, jt, true );
	}

	@Override
	public Path get(SingularAttribute attribute) {
		return (Path) get( attribute.getName(), JoinType.INNER, false );
	}

	@Override
	public Expression get(PluralAttribute collection) {
		return get( collection.getName(), JoinType.INNER, false );
	}

	@Override
	public Expression get(MapAttribute map) {
		return get( map.getName(), JoinType.INNER, false );
	}

	@Override
	public Path get(String attributeName) {
		return (Path) get( attributeName, JoinType.INNER, false );
	}

	@Override
	public Bindable getModel() {
		return (Bindable) getNavigableReference().getReferencedNavigable();
	}

	@Override
	public Expression<Class> type() {
		return new SqmEntityTypeExpression( getNavigableReference(), getSessionFactory() );
	}

	@Override
	public ExpressableType getExpressableType() {
		return getNavigableReference().getExpressableType();
	}

	@Override
	public ExpressableType getInferableType() {
		return getNavigableReference().getInferableType();
	}
}
