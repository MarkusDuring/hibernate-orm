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
import org.hibernate.type.descriptor.java.MutabilityPlan;

/**
 * @author Christian Beikov
 */
public class SqmLiteralGeneric<E> extends AbstractSqmLiteral<E> {
	private final MutabilityPlan<E> mutabilityPlan;

	public SqmLiteralGeneric(SessionFactoryImplementor sessionFactory, E value, BasicValuedExpressableType sqmExpressableTypeBasic, MutabilityPlan<E> mutabilityPlan) {
		super( sessionFactory, value, sqmExpressableTypeBasic );
		this.mutabilityPlan = mutabilityPlan;
	}

	@Override
	public SqmLiteralGeneric copy(SqmCopyContext context) {
		if ( mutabilityPlan.isMutable() ) {
			return new SqmLiteralGeneric<>(
                    getSessionFactory(),
					mutabilityPlan.deepCopy( getLiteralValue() ),
					getExpressableType(),
					mutabilityPlan
			);
		} else {
			return this;
		}
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralGenericExpression( this );
	}

	public MutabilityPlan<E> getMutabilityPlan() {
		return mutabilityPlan;
	}
}
