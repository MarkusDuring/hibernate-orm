/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.QueryException;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;


/**
 * Models a tuple of values, generally defined as a series of values
 * wrapped in parentheses, e.g. `(value1, value2, ..., valueN)`
 *
 * @author Steve Ebersole
 */
public class SqmTuple extends AbstractSqmExpression implements SqmExpression {
	private final List<SqmExpression> groupedExpressions;

	public SqmTuple(SessionFactoryImplementor sessionFactory, SqmExpression groupedExpression) {
		this( sessionFactory, Collections.singletonList( groupedExpression ) );
	}

	public SqmTuple(SessionFactoryImplementor sessionFactory, SqmExpression... groupedExpressions) {
		this( sessionFactory, Arrays.asList( groupedExpressions ));
	}

	private SqmTuple(SessionFactoryImplementor sessionFactory, List<SqmExpression> groupedExpressions) {
		super( sessionFactory );
		if ( groupedExpressions.isEmpty() ) {
			throw new QueryException( "tuple grouping cannot be constructed over zero expressions" );
		}
		this.groupedExpressions = groupedExpressions;
	}

	@Override
	public ExpressableType getExpressableType() {
		final Optional<SqmExpression> first = groupedExpressions.stream()
				.filter( sqmExpression -> sqmExpression.getExpressableType() != null )
				.findFirst();
		if ( !first.isPresent() ) {
			return null;
		}

		return first.get().getExpressableType();
	}

	@Override
	public ExpressableType getInferableType() {
		return null;
	}

	@Override
	public SqmTuple copy(SqmCopyContext context) {
		List<SqmExpression> newGroupedExpressions = new ArrayList<>( groupedExpressions.size() );
		for ( SqmExpression groupedExpression : groupedExpressions ) {
			newGroupedExpressions.add( groupedExpression.copy( context ) );
		}

		return new SqmTuple(
                getSessionFactory(),
				newGroupedExpressions
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return null;
	}

	@Override
	public String asLoggableText() {
		return null;
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getExpressableType().getJavaTypeDescriptor();
	}

//	@Override
//	public QueryResult createQueryResult(
//			SemanticQueryWalker walker,
//			String resultVariable,
//			QueryResultCreationContext creationContext) {
//		return null;
//	}
//
//	@Override
//	public QueryResult createQueryResult(
//			Expression expression,
//			String resultVariable,
//			QueryResultCreationContext creationContext) {
//		// todo (6.0) : pretty sure this is not correct.
//		//		should return a result over all the expressions, not just the first -
//		//		a "composite" result.
//		//
//		// todo (6.0) : ultimately the problem here is expecting the "resolved" SQL AST node to be passed in.
//		//		really resolving these SQL AST nodes should be done here.
//		return groupedExpressions.get( 0 ).createQueryResult( expression, resultVariable, creationContext );
//	}

}
