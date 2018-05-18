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
public class SqmExtractFunction extends AbstractSqmFunction {
	public static final String NAME = "extract";

	private final SqmExpression unitToExtract;
	private final SqmExpression extractionSource;

	public SqmExtractFunction(
			SessionFactoryImplementor sessionFactory,
			SqmExpression unitToExtract,
			SqmExpression extractionSource,
			AllowableFunctionReturnType resultType) {
		super( sessionFactory, resultType );
		this.unitToExtract = unitToExtract;
		this.extractionSource = extractionSource;
	}

	public SqmExpression getUnitToExtract() {
		return unitToExtract;
	}

	public SqmExpression getExtractionSource() {
		return extractionSource;
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
	public SqmExtractFunction copy(SqmCopyContext context) {
		return new SqmExtractFunction(
                getSessionFactory(),
				unitToExtract.copy( context ),
				extractionSource.copy( context ),
				getExpressableType()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitExtractFunction( this );
	}

	@Override
	public String asLoggableText() {
		return String.format(
				Locale.ROOT,
				"%s( %s from %s )",
				NAME,
				getUnitToExtract().asLoggableText(),
				getExtractionSource().asLoggableText()
		);
	}
}
