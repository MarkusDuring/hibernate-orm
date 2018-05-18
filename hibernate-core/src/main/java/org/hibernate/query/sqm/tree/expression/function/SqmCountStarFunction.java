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
import org.hibernate.query.sqm.tree.expression.AbstractSqmExpression;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class SqmCountStarFunction extends AbstractSqmAggregateFunction {
	public SqmCountStarFunction(SessionFactoryImplementor sessionFactory, AllowableFunctionReturnType resultType) {
		super( sessionFactory, STAR, resultType );
	}

	@Override
	public String getFunctionName() {
		return SqmCountFunction.NAME;
	}

	@Override
	public SqmCountStarFunction copy(SqmCopyContext context) {
		return this;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCountStarFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "COUNT(*)";
	}

	public static SqmExpression STAR = new AbstractSqmExpression( null ) {
		@Override
		public JavaTypeDescriptor getJavaTypeDescriptor() {
			throw new UnsupportedOperationException( "Illegal attempt to visit * as argument of count(*)" );
		}

		@Override
		public BasicValuedExpressableType getExpressableType() {
			throw new UnsupportedOperationException( "Illegal attempt to visit * as argument of count(*)" );
		}

		@Override
		public BasicValuedExpressableType getInferableType() {
			throw new UnsupportedOperationException( "Illegal attempt to visit * as argument of count(*)" );
		}

		@Override
		public <T> T accept(SemanticQueryWalker<T> walker) {
			throw new UnsupportedOperationException( "Illegal attempt to visit * as argument of count(*)" );
		}

		@Override
		public AbstractSqmExpression copy(SqmCopyContext context) {
			return this;
		}

		@Override
		public Class getJavaType() {
			return Long.class;
		}

		@Override
		public String asLoggableText() {
			return "*";
		}
	};
}
