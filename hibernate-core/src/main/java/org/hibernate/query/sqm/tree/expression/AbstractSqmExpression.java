/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.function.SqmCastFunction;
import org.hibernate.query.sqm.tree.select.SqmSelectableNode;
import org.hibernate.query.sqm.tree.select.SqmSelectionBase;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.Collection;

/**
 * @author Christian Beikov
 */
public abstract class AbstractSqmExpression extends SqmSelectionBase implements SqmExpression, Expression {

	private final SessionFactoryImplementor sessionFactory;

	public AbstractSqmExpression(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public abstract AbstractSqmExpression copy(SqmCopyContext context);

	@Override
	public Class getJavaType() {
		return getJavaTypeDescriptor().getJavaType();
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getExpressableType().getJavaTypeDescriptor();
	}

	@Override
	public SqmSelectableNode getSelectableNode() {
		return this;
	}

	@Override
	public Predicate isNull() {
		return getCriteriaBuilder().isNull( this );
	}

	@Override
	public Predicate isNotNull() {
		return getCriteriaBuilder().isNotNull( this );
	}

	@Override
	public Predicate in(Object... values) {
		return getCriteriaBuilder().in( this, values );
	}

	@Override
	public Predicate in(Expression[] values) {
		return getCriteriaBuilder().in( this, values );
	}

	@Override
	public Predicate in(Collection values) {
		return getCriteriaBuilder().in( this, values );
	}

	@Override
	public Predicate in(Expression values) {
		return getCriteriaBuilder().in( this ).value( values );
	}

	@Override
	public Expression as(Class type) {
		return type.equals( getJavaType() )
				? this
				: new SqmCastFunction(
						sessionFactory,
						this,
						sessionFactory.getTypeConfiguration()
								.getBasicTypeRegistry()
								.getBasicType(type)
				);
	}

	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	public HibernateCriteriaBuilder getCriteriaBuilder() {
		return sessionFactory.getCriteriaBuilder();
	}
}
