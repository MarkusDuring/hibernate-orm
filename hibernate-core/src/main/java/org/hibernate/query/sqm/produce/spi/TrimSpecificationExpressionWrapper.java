/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.produce.spi;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.sql.ast.tree.spi.TrimSpecification;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.Collection;
import java.util.List;

/**
 * Needed to pass TrimSpecification as an SqmExpression when we call out to
 * SqmFunctionTemplates handling TRIM calls.
 *
 * @author Steve Ebersole
 */
public class TrimSpecificationExpressionWrapper implements SqmExpression {
	private final TrimSpecification specification;

	private TrimSpecificationExpressionWrapper(TrimSpecification specification) {
		this.specification = specification;
	}

	public TrimSpecification getSpecification() {
		return specification;
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
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return null;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String asLoggableText() {
		return specification.name();
	}

	public static TrimSpecificationExpressionWrapper wrap(TrimSpecification specification) {
		return new TrimSpecificationExpressionWrapper( specification );
	}

	@Override
	public SqmExpression copy(SqmCopyContext context) {
		return this;
	}

	@Override
	public Predicate isNull() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Predicate isNotNull() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Predicate in(Object... values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Predicate in(Expression[] values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Predicate in(Collection values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Predicate in(Expression values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Expression as(Class type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Selection alias(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCompoundSelection() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class getJavaType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAlias() {
		throw new UnsupportedOperationException();
	}
}
