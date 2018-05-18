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
import org.hibernate.query.sqm.tree.expression.AbstractSqmExpression;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Ebersole
 */
public class SqmCoalesceFunction extends AbstractSqmExpression implements SqmFunction, CriteriaBuilder.Coalesce {
	public static final String NAME = "coalesce";

	private final List<SqmExpression> arguments;
	private AllowableFunctionReturnType resultType;

	public SqmCoalesceFunction(SessionFactoryImplementor sessionFactory) {
		super( sessionFactory );
		this.arguments = new ArrayList<>();
	}

	public SqmCoalesceFunction(SessionFactoryImplementor sessionFactory, AllowableFunctionReturnType resultType, List<SqmExpression> arguments) {
		super( sessionFactory );
		this.resultType = resultType;
		this.arguments = arguments;
	}

	@Override
	public AllowableFunctionReturnType getExpressableType() {
		return resultType;
	}

	@Override
	public ExpressableType getInferableType() {
		return getExpressableType();
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		if ( getExpressableType() == null ) {
			return null;
		}

		return getExpressableType().getJavaTypeDescriptor();
	}

	public List<SqmExpression> getArguments() {
		return arguments;
	}

	public void value(SqmExpression expression) {
		arguments.add( expression );

		if ( resultType == null ) {
			resultType = (AllowableFunctionReturnType) expression.getExpressableType();
		}
	}

	@Override
	public SqmCoalesceFunction copy(SqmCopyContext context) {
		List<SqmExpression> newArguments = new ArrayList<>( arguments.size() );
		for ( SqmExpression argument : arguments ) {
			newArguments.add( argument.copy( context ) );
		}

		return new SqmCoalesceFunction(
                getSessionFactory(),
				resultType,
				newArguments
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCoalesceFunction( this );
	}

	@Override
	public CriteriaBuilder.Coalesce value(Object value) {
		value( getCriteriaBuilder().literal( value ) );
		return this;
	}

	@Override
	public CriteriaBuilder.Coalesce value(Expression value) {
		value( (SqmExpression) value );
		return this;
	}

	@Override
	public String asLoggableText() {
		return "coalesce(...)";
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}
}
