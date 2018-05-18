/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.function;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.AllowableFunctionReturnType;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.AbstractSqmExpression;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmFunction extends AbstractSqmExpression implements SqmFunction {
	private final AllowableFunctionReturnType resultType;

	public AbstractSqmFunction(SessionFactoryImplementor sessionFactory, AllowableFunctionReturnType resultType) {
		super( sessionFactory );
		this.resultType = resultType;
	}

	@Override
	public abstract AbstractSqmFunction copy(SqmCopyContext context);

	@Override
	public AllowableFunctionReturnType getExpressableType() {
		return resultType;
	}

	@Override
	public AllowableFunctionReturnType getInferableType() {
		return getExpressableType();
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getExpressableType().getJavaTypeDescriptor();
	}
}
