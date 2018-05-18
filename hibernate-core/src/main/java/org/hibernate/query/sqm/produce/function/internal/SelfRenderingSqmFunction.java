/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.produce.function.internal;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.AllowableFunctionReturnType;
import org.hibernate.query.sqm.produce.function.spi.SelfRenderingFunctionSupport;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.AbstractSqmExpression;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.sql.ast.produce.spi.SqlAstFunctionProducer;
import org.hibernate.sql.ast.produce.sqm.spi.SqmToSqlAstConverter;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Ebersole
 */
public class SelfRenderingSqmFunction extends AbstractSqmExpression implements SqlAstFunctionProducer {
	private final SelfRenderingFunctionSupport renderingSupport;
	private final List<SqmExpression> sqmArguments;
	private final AllowableFunctionReturnType impliedResultType;

	public SelfRenderingSqmFunction(
			SessionFactoryImplementor sessionFactory,
			SelfRenderingFunctionSupport renderingSupport,
			List<SqmExpression> sqmArguments,
			AllowableFunctionReturnType impliedResultType) {
		super( sessionFactory );
		this.renderingSupport = renderingSupport;
		this.sqmArguments = sqmArguments;
		this.impliedResultType = impliedResultType;
	}

	public SelfRenderingSqmFunction(
			SessionFactoryImplementor sessionFactory,
			List<SqmExpression> sqmArguments,
			AllowableFunctionReturnType impliedResultType) {
		super( sessionFactory );
		this.renderingSupport = null;
		this.sqmArguments = sqmArguments;
		this.impliedResultType = impliedResultType;
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getExpressableType().getJavaTypeDescriptor();
	}

	@Override
	public AllowableFunctionReturnType getExpressableType() {
		return impliedResultType;
	}

	public SelfRenderingFunctionSupport getRenderingSupport() {
		return renderingSupport;
	}

	public List<SqmExpression> getSqmArguments() {
		return sqmArguments;
	}

	@Override
	public SelfRenderingSqmFunction copy(SqmCopyContext context) {
		List<SqmExpression> newArguments = new ArrayList<>( sqmArguments.size() );
		for ( SqmExpression argument : sqmArguments ) {
			newArguments.add( argument.copy( context ) );
		}

		return new SelfRenderingSqmFunction(
                getSessionFactory(),
				renderingSupport,
				newArguments,
				impliedResultType
		);
	}

	@Override
	public Expression convertToSqlAst(SqmToSqlAstConverter walker) {
		return new SelfRenderingFunctionSqlAstExpression( this, walker );
	}
}
