/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * Models a positional parameter expression
 *
 * @author Steve Ebersole
 */
public class SqmPositionalParameter extends AbstractSqmExpression implements SqmParameter {
	private final int position;
	private final boolean canBeMultiValued;
	private final Class<?> javaType;
	private ExpressableType expressableType;

	public SqmPositionalParameter(SessionFactoryImplementor sessionFactory, int position, boolean canBeMultiValued) {
		super( sessionFactory );
		this.position = position;
		this.canBeMultiValued = canBeMultiValued;
		this.javaType = null;
	}

	public SqmPositionalParameter(SessionFactoryImplementor sessionFactory, int position, boolean canBeMultiValued, Class<?> javaType) {
		super( sessionFactory );
		this.position = position;
		this.canBeMultiValued = canBeMultiValued;
		this.javaType = javaType;
	}

	private SqmPositionalParameter(SessionFactoryImplementor sessionFactory, int position, boolean canBeMultiValued, Class<?> javaType, ExpressableType expressableType) {
		super( sessionFactory );
		this.position = position;
		this.canBeMultiValued = canBeMultiValued;
		this.javaType = javaType;
		this.expressableType = expressableType;
	}

	@Override
	public ExpressableType getExpressableType() {
		return expressableType;
	}

	@Override
	public ExpressableType getInferableType() {
		return getExpressableType();
	}

	@Override
	public SqmPositionalParameter copy(SqmCopyContext context) {
		return new SqmPositionalParameter(
                getSessionFactory(),
				position,
				canBeMultiValued,
				javaType,
				expressableType
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitPositionalParameterExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "?" + getPosition();
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Integer getPosition() {
		return position;
	}

	@Override
	public void impliedType(ExpressableType expressableType) {
		if ( expressableType != null ) {
			this.expressableType = expressableType;
		}
	}

	@Override
	public Class getParameterType() {
		if ( javaType != null ) {
			return javaType;
		}
		return getJavaTypeDescriptor().getJavaType();
	}

	@Override
	public boolean allowMultiValuedBinding() {
		return canBeMultiValued;
	}

	@Override
	public ExpressableType getAnticipatedType() {
		return getExpressableType();
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return expressableType.getJavaTypeDescriptor();
	}
}
