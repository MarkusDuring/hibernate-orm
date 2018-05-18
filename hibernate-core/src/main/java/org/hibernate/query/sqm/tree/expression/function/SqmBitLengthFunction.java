/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.function;

import java.util.Locale;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.AllowableFunctionReturnType;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class SqmBitLengthFunction extends AbstractSqmFunction {
	public static final String NAME = "bit_length";

	private final SqmExpression argument;

	public SqmBitLengthFunction(SessionFactoryImplementor sessionFactory, SqmExpression argument) {
		this( sessionFactory, argument, (AllowableFunctionReturnType) argument.getExpressableType() );
	}

	private SqmBitLengthFunction(
			SessionFactoryImplementor sessionFactory,
			SqmExpression argument,
			AllowableFunctionReturnType resultType) {
		super( sessionFactory, resultType );
		this.argument = argument;
	}

	public SqmExpression getArgument() {
		return argument;
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public SqmBitLengthFunction copy(SqmCopyContext context) {
		return new SqmBitLengthFunction(
				getSessionFactory(),
				argument.copy( context ),
				getExpressableType()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitBitLengthFunction( this );
	}

	@Override
	public String asLoggableText() {
		return String.format(
				Locale.ROOT,
				"%s( %s )",
				NAME,
				argument.asLoggableText()
		);
	}
}
