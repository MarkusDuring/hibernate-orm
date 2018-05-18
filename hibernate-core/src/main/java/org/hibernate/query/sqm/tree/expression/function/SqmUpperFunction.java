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
public class SqmUpperFunction extends AbstractSqmFunction {
	public static final String NAME = "upper";

	private SqmExpression argument;

	public SqmUpperFunction(SessionFactoryImplementor sessionFactory, BasicValuedExpressableType resultType, SqmExpression argument) {
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
	public SqmUpperFunction copy(SqmCopyContext context) {
		return new SqmUpperFunction(
                getSessionFactory(),
				(BasicValuedExpressableType) getExpressableType(),
				argument.copy( context )
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitUpperFunction( this );
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
