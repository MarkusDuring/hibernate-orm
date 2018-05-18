/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.function;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.AllowableFunctionReturnType;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;

/**
 * @author Steve Ebersole
 */
public class SqmMaxFunction
		extends AbstractSqmAggregateFunction
		implements SqmAggregateFunction {
	public static final String NAME = "max";

	public SqmMaxFunction(SessionFactoryImplementor sessionFactory, SqmExpression argument) {
		super( sessionFactory, argument, (AllowableFunctionReturnType) argument.getExpressableType() );
	}

	public SqmMaxFunction(SessionFactoryImplementor sessionFactory, SqmExpression argument, BasicValuedExpressableType resultType) {
		super( sessionFactory, argument, resultType );
	}

	private SqmMaxFunction(SessionFactoryImplementor sessionFactory, SqmExpression argument, AllowableFunctionReturnType resultType, boolean distinct) {
		super( sessionFactory, argument, resultType, distinct );
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	@Override
	public SqmMaxFunction copy(SqmCopyContext context) {
		return new SqmMaxFunction(
				getSessionFactory(),
				getArgument().copy( context ),
				getExpressableType(),
				isDistinct()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMaxFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "MAX(" + getArgument().asLoggableText() + ")";
	}
}
