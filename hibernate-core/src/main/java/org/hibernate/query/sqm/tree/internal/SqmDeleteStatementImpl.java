/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.internal;

import java.util.Locale;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmDeleteStatement;
import org.hibernate.query.sqm.tree.SqmNode;
import org.hibernate.query.sqm.tree.SqmStatement;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;

/**
 * @author Steve Ebersole
 */
public class SqmDeleteStatementImpl extends AbstractSqmStatement implements SqmDeleteStatement {
	private final SqmRoot entityFromElement;
	private final SqmWhereClause whereClause;

	public SqmDeleteStatementImpl(SqmCreationContext creationContext, SqmRoot entityFromElement) {
		super( creationContext );
		this.entityFromElement = entityFromElement;
		this.whereClause = new SqmWhereClause();
	}

	public SqmDeleteStatementImpl(SqmCreationContext creationContext, Class<?> entityClass) {
		super( creationContext );
		this.entityFromElement = createRoot(null, entityClass );
		this.whereClause = new SqmWhereClause();
	}

	private SqmDeleteStatementImpl(SqmCreationContext creationContext, SqmRoot entityFromElement, SqmWhereClause whereClause) {
		super( creationContext );
		this.entityFromElement = entityFromElement;
		this.whereClause = whereClause;
	}

	@Override
	public SqmDeleteStatement copy() {
		return (SqmDeleteStatement) super.copy();
	}

	@Override
	public SqmDeleteStatement copy(SqmCopyContext context) {
		// First register the copy instance so that subqueries can look it up
		SqmDeleteStatementImpl statement = new SqmDeleteStatementImpl(
				context.getCreationContext(),
				entityFromElement.copy( context ),
				new SqmWhereClause()
		);
		// only then copy the query spec
		statement.whereClause.copyFrom( whereClause, context );
		return statement;
	}

	@Override
	public SqmRoot getEntityFromElement() {
		return entityFromElement;
	}

	public SqmWhereClause getWhereClause() {
		return whereClause;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"delete %s %s",
				entityFromElement,
				whereClause
		);
	}

	@Override
	public Root from(Class entityClass) {
		if ( entityClass != entityFromElement.getJavaType() ) {
			throw new IllegalArgumentException( "invalid from type [" + entityClass + "], expected [" + entityFromElement.getJavaType() + "]" );
		}
		return entityFromElement;
	}

	@Override
	public Root from(EntityType entity) {
		return from( entity.getJavaType() );
	}

	@Override
	public Root getRoot() {
		return entityFromElement;
	}

	@Override
	public CriteriaDelete where(Expression restriction) {
		whereClause.setPredicate( (SqmPredicate) criteriaBuilder().wrap( restriction ) );
		return this;
	}

	@Override
	public CriteriaDelete where(Predicate... restrictions) {
		whereClause.setPredicate( (SqmPredicate) criteriaBuilder().and( restrictions ) );
		return this;
	}

	@Override
	public Predicate getRestriction() {
		return whereClause.getPredicate();
	}
}
