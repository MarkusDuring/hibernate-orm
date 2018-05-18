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
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class SqmUnaryOperation extends AbstractSqmExpression implements ImpliedTypeSqmExpression {

	public enum Operation {
		PLUS,
		MINUS
	}

	private final Operation operation;
	private final SqmExpression operand;

	private BasicValuedExpressableType typeDescriptor;

	public SqmUnaryOperation(SessionFactoryImplementor sessionFactory, Operation operation, SqmExpression operand) {
		this( sessionFactory, operation, operand, (BasicValuedExpressableType) operand.getExpressableType() );
	}

	private SqmUnaryOperation(SessionFactoryImplementor sessionFactory, Operation operation, SqmExpression operand, BasicValuedExpressableType typeDescriptor) {
		super( sessionFactory );
		this.operation = operation;
		this.operand = operand;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public BasicValuedExpressableType getExpressableType() {
		return typeDescriptor;
	}

	@Override
	public BasicValuedExpressableType getInferableType() {
		return (BasicValuedExpressableType) operand.getExpressableType();
	}

	@Override
	public void impliedType(ExpressableType type) {
		if ( type != null ) {
			this.typeDescriptor = (BasicValuedExpressableType) type;
			if ( operand instanceof ImpliedTypeSqmExpression ) {
				( (ImpliedTypeSqmExpression) operand ).impliedType( type );
			}
		}
	}

	@Override
	public SqmUnaryOperation copy(SqmCopyContext context) {
		return new SqmUnaryOperation(
                getSessionFactory(),
				operation,
				operand.copy( context ),
				typeDescriptor
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitUnaryOperationExpression( this );
	}

	@Override
	public String asLoggableText() {
		return ( operation == Operation.MINUS ? '-' : '+' ) + operand.asLoggableText();
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return typeDescriptor.getJavaTypeDescriptor();
	}

	public SqmExpression getOperand() {
		return operand;
	}

	public Operation getOperation() {
		return operation;
	}
}
