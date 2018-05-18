/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.function;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;

import java.util.Locale;

/**
 * @author Steve Ebersole
 */
public class SqmLowerFunction extends AbstractSqmFunction {
	public static final String NAME = "lower";

	private SqmExpression argument;

	public SqmLowerFunction(SessionFactoryImplementor sessionFactory, BasicValuedExpressableType resultType, SqmExpression argument) {
		super( sessionFactory, resultType );
		this.argument = argument;

		assert argument != null;
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	public SqmExpression getArgument() {
		return argument;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public SqmLowerFunction copy(SqmCopyContext context) {
		return new SqmLowerFunction(
                getSessionFactory(),
				(BasicValuedExpressableType) getExpressableType(),
				argument.copy( context )
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLowerFunction( this );
	}

	@Override
	public String asLoggableText() {
		return String.format(
				Locale.ROOT,
				"%s( %s )",
				NAME,
				getArgument().asLoggableText()
		);
	}
}
