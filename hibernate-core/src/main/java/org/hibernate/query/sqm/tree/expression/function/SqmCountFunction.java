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

/**
 * @author Steve Ebersole
 */
public class SqmCountFunction extends AbstractSqmAggregateFunction {
	public static final String NAME = "count";

	public SqmCountFunction(SessionFactoryImplementor sessionFactory, SqmExpression argument, AllowableFunctionReturnType resultType) {
		super( sessionFactory, argument, resultType );
	}

	public SqmCountFunction(SessionFactoryImplementor sessionFactory, SqmExpression argument, AllowableFunctionReturnType resultType, boolean distinct) {
		super( sessionFactory, argument, resultType, distinct );
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	@Override
	public SqmCountFunction copy(SqmCopyContext context) {
		return new SqmCountFunction(
				getSessionFactory(),
				getArgument().copy( context ),
				getExpressableType(),
				isDistinct()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCountFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "COUNT(" + getArgument().asLoggableText() + ")";
	}
}
