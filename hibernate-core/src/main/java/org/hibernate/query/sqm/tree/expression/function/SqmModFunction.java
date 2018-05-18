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

import java.util.Locale;

/**
 * @author Steve Ebersole
 */
public class SqmModFunction extends AbstractSqmFunction {
	public static final String NAME = "mod";

	private final SqmExpression dividend;
	private final SqmExpression divisor;

	public SqmModFunction(
			SessionFactoryImplementor sessionFactory,
			SqmExpression dividend,
			SqmExpression divisor,
			AllowableFunctionReturnType resultType) {
		super( sessionFactory, resultType );
		this.dividend = dividend;
		this.divisor = divisor;
	}

	public SqmExpression getDividend() {
		return dividend;
	}

	public SqmExpression getDivisor() {
		return divisor;
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
	public SqmModFunction copy(SqmCopyContext context) {
		return new SqmModFunction(
                getSessionFactory(),
				dividend.copy( context ),
				divisor.copy( context ),
				getExpressableType()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitModFunction( this );
	}

	@Override
	public String asLoggableText() {
		return String.format(
				Locale.ROOT,
				"%s( %s, %s )",
				NAME,
				dividend.asLoggableText(),
				divisor.asLoggableText()
		);
	}
}
