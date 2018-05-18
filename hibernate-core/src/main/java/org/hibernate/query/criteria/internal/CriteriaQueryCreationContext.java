/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.query.sqm.produce.internal.QuerySpecProcessingStateDmlImpl;
import org.hibernate.query.sqm.produce.internal.QuerySpecProcessingStateStandardImpl;
import org.hibernate.query.sqm.produce.internal.UniqueIdGenerator;
import org.hibernate.query.sqm.produce.spi.*;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableContainerReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;

import javax.persistence.criteria.CommonAbstractCriteria;

/**
 *
 * @author Christian Beikov
 */
public class CriteriaQueryCreationContext implements SqmCreationContext, CurrentSqmFromElementSpaceCoordAccess {
	private final SessionFactoryImplementor sessionFactory;
	private final QuerySpecProcessingState querySpecProcessingState;
	private final ImplicitAliasGenerator implicitAliasGenerator;
	private final UniqueIdGenerator uidGenerator;

	private CommonAbstractCriteria containingQuery;
	private SqmFromBuilder fromBuilder;
	private SqmFromElementSpace currentSqmFromElementSpace;

	// Since there are no named constructors, we have to workaround by using unique signatures

	private CriteriaQueryCreationContext(SessionFactoryImplementor sessionFactory, boolean a) {
		this.sessionFactory = sessionFactory;
		this.querySpecProcessingState = new QuerySpecProcessingStateStandardImpl( this, null );
		this.implicitAliasGenerator = new ImplicitAliasGenerator();
		this.uidGenerator = new UniqueIdGenerator();
	}

	private CriteriaQueryCreationContext(SessionFactoryImplementor sessionFactory, int a) {
		this.sessionFactory = sessionFactory;
		this.querySpecProcessingState = new QuerySpecProcessingStateDmlImpl( this );
		this.implicitAliasGenerator = new ImplicitAliasGenerator();
		this.uidGenerator = new UniqueIdGenerator();
	}

	public static SqmCreationContext forQuery(SessionFactoryImplementor sessionFactory) {
		return new CriteriaQueryCreationContext( sessionFactory, false );
	}

	public static SqmCreationContext forDml(SessionFactoryImplementor sessionFactory) {
		return new CriteriaQueryCreationContext( sessionFactory, 0 );
	}

	@Override
	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public QuerySpecProcessingState getCurrentQuerySpecProcessingState() {
		return querySpecProcessingState;
	}

	@Override
	public SqmFromElementSpace getCurrentFromElementSpace() {
		return currentSqmFromElementSpace;
	}

	@Override
	public void setCurrentSqmFromElementSpace(SqmFromElementSpace space) {
		currentSqmFromElementSpace = space;
	}

	@Override
	public SqmFromBuilder getCurrentFromElementBuilder() {
		return fromBuilder;
	}

	@Override
	public void setCurrentFromElementBuilder(SqmFromBuilder fromBuilder) {
		this.fromBuilder = fromBuilder;
	}

	@Override
	public CommonAbstractCriteria getCurrentContainingQuery() {
		return containingQuery;
	}

	@Override
	public void setCurrentContainingQuery(CommonAbstractCriteria currentContainingQuery) {
		this.containingQuery = currentContainingQuery;
	}

	@Override
	public CurrentSqmFromElementSpaceCoordAccess getCurrentSqmFromElementSpaceCoordAccess() {
		return this;
	}

	@Override
	public String generateUniqueIdentifier() {
		return uidGenerator.generateUniqueId();
	}

	@Override
	public ImplicitAliasGenerator getImplicitAliasGenerator() {
		return implicitAliasGenerator;
	}

	@Override
	public void cacheNavigableReference(SqmNavigableReference reference) {

	}

	@Override
	public SqmNavigableReference getCachedNavigableReference(SqmNavigableContainerReference source, Navigable navigable) {
		return null;
	}
}
