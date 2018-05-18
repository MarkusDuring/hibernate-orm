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
import org.hibernate.type.spi.StandardSpiBasicTypes;

/**
 * @author Steve Ebersole
 */
public class SqmLiteralInteger extends AbstractSqmLiteral<Integer> {
	public SqmLiteralInteger(SessionFactoryImplementor sessionFactory, Integer value, BasicValuedExpressableType sqmExpressableTypeBasic) {
		super( sessionFactory, value, sqmExpressableTypeBasic );
	}

	public SqmLiteralInteger(SessionFactoryImplementor sessionFactory, int i) {
		this( sessionFactory, i, StandardSpiBasicTypes.INTEGER );
	}

	@Override
	public SqmLiteralInteger copy(SqmCopyContext context) {
		return this;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralIntegerExpression( this );
	}
}
