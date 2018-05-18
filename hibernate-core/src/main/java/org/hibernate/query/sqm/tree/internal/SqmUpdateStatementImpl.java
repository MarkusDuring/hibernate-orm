/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.internal;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmStatement;
import org.hibernate.query.sqm.tree.SqmUpdateStatement;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReference;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;
import org.hibernate.query.sqm.tree.set.SqmAssignment;
import org.hibernate.query.sqm.tree.set.SqmSetClause;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Locale;

/**
 * @author Steve Ebersole
 */
public class SqmUpdateStatementImpl extends AbstractSqmStatement implements SqmUpdateStatement {
	private final SqmRoot entityFromElement;
	private final SqmSetClause setClause;
	private final SqmWhereClause whereClause;

	public SqmUpdateStatementImpl(SqmCreationContext creationContext, SqmRoot entityFromElement) {
		super( creationContext );
		this.entityFromElement = entityFromElement;
		this.setClause = new SqmSetClause();
		this.whereClause = new SqmWhereClause();
	}

	public SqmUpdateStatementImpl(SqmCreationContext creationContext, Class<?> entityClass) {
		super( creationContext );
		this.entityFromElement = createRoot(null, entityClass );
		this.setClause = new SqmSetClause();
		this.whereClause = new SqmWhereClause();
	}

	private SqmUpdateStatementImpl(SqmCreationContext creationContext, SqmRoot entityFromElement, SqmSetClause setClause, SqmWhereClause whereClause) {
		super( creationContext );
		this.entityFromElement = entityFromElement;
		this.setClause = setClause;
		this.whereClause = whereClause;
	}

	@Override
	public SqmUpdateStatement copy() {
		return (SqmUpdateStatement) super.copy();
	}

	@Override
	public SqmUpdateStatement copy(SqmCopyContext context) {
		// First register the copy instance so that subqueries can look it up
		SqmUpdateStatementImpl statement = new SqmUpdateStatementImpl(
				context.getCreationContext(),
				entityFromElement.copy( context ),
				new SqmSetClause(),
				new SqmWhereClause()
		);
		// only then copy the query spec
		statement.setClause.copyFrom( setClause, context );
		statement.whereClause.copyFrom( whereClause, context );
		return statement;
	}

	@Override
	public SqmRoot getEntityFromElement() {
		return entityFromElement;
	}

	@Override
	public SqmSetClause getSetClause() {
		return setClause;
	}

	@Override
	public SqmWhereClause getWhereClause() {
		return whereClause;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"update %s %s %s",
				entityFromElement,
				"[no set clause]",
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
	public CriteriaUpdate set(SingularAttribute attribute, Object value) {
		setClause.addAssignment( new SqmAssignment( (SqmSingularAttributeReference) attribute, (SqmExpression) criteriaBuilder().literal( value ) ) );
		return this;
	}

	@Override
	public CriteriaUpdate set(SingularAttribute attribute, Expression value) {
		setClause.addAssignment( new SqmAssignment( (SqmSingularAttributeReference) attribute, (SqmExpression) value ) );
		return this;
	}

	@Override
	public CriteriaUpdate set(Path attribute, Object value) {
		setClause.addAssignment( new SqmAssignment( (SqmSingularAttributeReference) attribute, (SqmExpression) criteriaBuilder().literal( value ) ) );
		return this;
	}

	@Override
	public CriteriaUpdate set(Path attribute, Expression value) {
		setClause.addAssignment( new SqmAssignment( (SqmSingularAttributeReference) attribute, (SqmExpression) value ) );
		return this;
	}

	@Override
	public CriteriaUpdate set(String attributeName, Object value) {
		setClause.addAssignment( new SqmAssignment( (SqmSingularAttributeReference) entityFromElement.get( attributeName ), (SqmExpression) criteriaBuilder().literal( value ) ) );
		return this;
	}

	@Override
	public CriteriaUpdate where(Expression restriction) {
		whereClause.setPredicate( (SqmPredicate) criteriaBuilder().wrap( restriction ) );
		return this;
	}

	@Override
	public CriteriaUpdate where(Predicate... restrictions) {
		whereClause.setPredicate( (SqmPredicate) criteriaBuilder().and( restrictions ) );
		return this;
	}

	@Override
	public Predicate getRestriction() {
		return whereClause.getPredicate();
	}
}
