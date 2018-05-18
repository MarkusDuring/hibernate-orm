/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;

/**
 * @author Steve Ebersole
 */
public class SqmLiteralFloat extends AbstractSqmLiteral<Float> {
	public SqmLiteralFloat(SessionFactoryImplementor sessionFactory, Float value, BasicValuedExpressableType sqmExpressableTypeBasic) {
		super( sessionFactory, value, sqmExpressableTypeBasic );
	}

	@Override
	public SqmLiteralFloat copy(SqmCopyContext context) {
		return this;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralFloatExpression( this );
	}
}
