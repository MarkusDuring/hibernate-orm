/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.internal;

import java.util.Locale;

import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.hibernate.query.criteria.spi.JpaCriteriaBuilderImplementor;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.produce.spi.criteria.JpaPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmDeleteStatement;
import org.hibernate.query.sqm.tree.from.SqmRoot;

/**
 * @author Steve Ebersole
 */
public class SqmDeleteStatementImpl extends AbstractSqmStatement implements SqmDeleteStatement {
	private SqmRoot entityFromElement;
	private final SqmWhereClause whereClause = new SqmWhereClause();

	public SqmDeleteStatementImpl(
			JpaCriteriaBuilderImplementor criteriaBuilder,
			ParsingContext parsingContext) {
		super( criteriaBuilder, parsingContext );
	}

	public SqmDeleteStatementImpl(
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
	public CriteriaDelete where(Expression restriction) {
		setWhere( whereClause, restriction );
		return this;
	}

	@Override
	public CriteriaDelete where(Predicate... restrictions) {
		setWhere( whereClause, restrictions );
		return this;
	}

	@Override
	public Predicate getRestriction() {
		return whereClause.getPredicate();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitDeleteStatement( this );
	}
}
