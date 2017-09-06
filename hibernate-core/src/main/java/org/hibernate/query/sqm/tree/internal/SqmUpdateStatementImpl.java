/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.internal;

import java.util.Locale;

import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.query.criteria.spi.JpaCriteriaBuilderImplementor;
import org.hibernate.query.criteria.spi.JpaExpressionImplementor;
import org.hibernate.query.criteria.spi.JpaPathImplementor;
import org.hibernate.query.criteria.spi.JpaPredicateImplementor;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReference;
import org.hibernate.query.sqm.tree.predicate.AndSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmUpdateStatement;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.set.SqmAssignment;
import org.hibernate.query.sqm.tree.set.SqmSetClause;

/**
 * @author Steve Ebersole
 */
public class SqmUpdateStatementImpl extends AbstractSqmStatement implements SqmUpdateStatement {
	private SqmRoot entityFromElement;
	private final SqmSetClause setClause = new SqmSetClause();
	private final SqmWhereClause whereClause = new SqmWhereClause();

	public SqmUpdateStatementImpl(
			JpaCriteriaBuilderImplementor criteriaBuilder,
			ParsingContext parsingContext) {
		super( criteriaBuilder, parsingContext );
	}

	public SqmUpdateStatementImpl(
			JpaCriteriaBuilderImplementor criteriaBuilder,
			ParsingContext parsingContext,
			SqmRoot entityFromElement) {
		super( criteriaBuilder, parsingContext );
		this.entityFromElement = entityFromElement;
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
		SqmRoot root = createRoot( entityClass );
		this.entityFromElement = root;
		return root;
	}

	@Override
	public Root from(EntityType entity) {
		SqmRoot root = createRoot( entity );
		this.entityFromElement = root;
		return root;
	}

	@Override
	public Root getRoot() {
		return entityFromElement;
	}

	@Override
	public CriteriaUpdate set(
			SingularAttribute attribute, Object value) {
		return set(
				getAttributeReference( entityFromElement, attribute.getName() ),
				createConstantExpression( value )
		);
	}

	@Override
	public CriteriaUpdate set(
			SingularAttribute attribute, Expression value) {
		return set(
				getAttributeReference( entityFromElement, attribute.getName() ),
				createConstantExpression( value )
		);
	}

	@Override
	public CriteriaUpdate set(Path attribute, Object value) {
		return set(
				( (JpaPathImplementor<?>) attribute ).getSqmNavigableReference(),
				createConstantExpression( value )
		);
	}

	@Override
	public CriteriaUpdate set(
			Path attribute, Expression value) {
		return set(
				( (JpaPathImplementor<?>) attribute ).getSqmNavigableReference(),
				( (JpaExpressionImplementor<?>) value ).getSqmExpression()
		);
	}

	@Override
	public CriteriaUpdate set(String attributeName, Object value) {
		return set( getAttributeReference( entityFromElement, attributeName ), createConstantExpression( value ) );
	}

	private CriteriaUpdate set(SqmNavigableReference stateField, SqmExpression value) {
		setClause.addAssignment( new SqmAssignment( (SqmSingularAttributeReference) stateField, value ) );
		return this;
	}

	@Override
	public CriteriaUpdate where(Expression restriction) {
		setWhere( whereClause, restriction );
		return this;
	}

	@Override
	public CriteriaUpdate where(Predicate... restrictions) {
		setWhere( whereClause, restrictions );
		return this;
	}

	@Override
	public Predicate getRestriction() {
		return whereClause.getPredicate();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitUpdateStatement( this );
	}
}
