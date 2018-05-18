/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.predicate;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.type.spi.BasicType;

/**
 * Represents an expression whose type is boolean, and can therefore be used as a predicate.
 *
 * @author Steve Ebersole
 */
public class BooleanExpressionSqmPredicate extends AbstractSqmPredicate implements SqmPredicate {
	private final SqmExpression booleanExpression;

	public BooleanExpressionSqmPredicate(SessionFactoryImplementor sessionFactory, SqmExpression booleanExpression) {
		super( sessionFactory );
		assert booleanExpression.getExpressableType() != null;
		final Class expressionJavaType = ( (BasicType) booleanExpression.getExpressableType() ).getJavaType();
		assert boolean.class.equals( expressionJavaType ) || Boolean.class.equals( expressionJavaType );

		this.booleanExpression = booleanExpression;
	}

	public SqmExpression getBooleanExpression() {
		return booleanExpression;
	}

	@Override
	public BooleanExpressionSqmPredicate copy(SqmCopyContext context) {
		return new BooleanExpressionSqmPredicate(
				sessionFactory,
				booleanExpression.copy( context )
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitBooleanExpressionPredicate( this );
	}
}
