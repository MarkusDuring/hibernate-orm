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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Ebersole
 */
public class SqmGenericFunction extends AbstractSqmFunction implements SqmNonStandardFunction {

	// todo (6.0) : rename this (and friends) using the "non-standard" wording

	private final String functionName;
	private final List<SqmExpression> arguments;

	public SqmGenericFunction(
			SessionFactoryImplementor sessionFactory,
			String functionName,
			AllowableFunctionReturnType resultType,
			List<SqmExpression> arguments) {
		super( sessionFactory, resultType );
		this.functionName = functionName;
		this.arguments = arguments;
	}

	public String getFunctionName() {
		return functionName;
	}

	@Override
	public boolean hasArguments() {
		return arguments != null && !arguments.isEmpty();
	}

	public List<SqmExpression> getArguments() {
		return arguments;
	}

	@Override
	public SqmGenericFunction copy(SqmCopyContext context) {
		List<SqmExpression> newArguments = new ArrayList<>( arguments.size() );
		for ( SqmExpression argument : arguments ) {
			newArguments.add( argument.copy( context ) );
		}

		return new SqmGenericFunction(
                getSessionFactory(),
				functionName,
				getExpressableType(),
				newArguments
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitGenericFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "function(" + getFunctionName() + " ...)";
	}
}
