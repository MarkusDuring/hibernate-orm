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
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Ebersole
 */
public class SqmCaseSearched extends AbstractSqmExpression implements ImpliedTypeSqmExpression, CriteriaBuilder.Case {
	private List<WhenFragment> whenFragments = new ArrayList<>();
	private SqmExpression otherwise;

	private ExpressableType expressableType;
	private ExpressableType impliedType;

	public SqmCaseSearched(SessionFactoryImplementor sessionFactory) {
		super( sessionFactory );
	}

	private SqmCaseSearched(
			SessionFactoryImplementor sessionFactory,
			List<WhenFragment> whenFragments,
			SqmExpression otherwise,
			ExpressableType expressableType,
			ExpressableType impliedType) {
		super( sessionFactory );
		this.whenFragments = whenFragments;
		this.otherwise = otherwise;
		this.expressableType = expressableType;
		this.impliedType = impliedType;
	}

	public List<WhenFragment> getWhenFragments() {
		return whenFragments;
	}

	public SqmExpression getOtherwise() {
		return otherwise;
	}

	public void when(SqmPredicate predicate, SqmExpression result) {
		whenFragments.add( new WhenFragment( predicate, result ) );
	}

	public void otherwise(SqmExpression otherwiseExpression) {
		this.otherwise = otherwiseExpression;
		// todo : inject implied type?
	}

	@Override
	public CriteriaBuilder.Case when(Expression condition, Object result) {
		when( (SqmPredicate) getCriteriaBuilder().wrap( condition ), (SqmExpression) getCriteriaBuilder().literal( result ) );
		return this;
	}

	@Override
	public CriteriaBuilder.Case when(Expression condition, Expression result) {
		when( (SqmPredicate) getCriteriaBuilder().wrap( condition ), (SqmExpression) result );
		return this;
	}

	@Override
	public Expression otherwise(Object result) {
		otherwise( getCriteriaBuilder().literal( result ) );
		return this;
	}

	@Override
	public Expression otherwise(Expression result) {
		otherwise( (SqmExpression) result );
		return this;
	}

	@Override
	public void impliedType(ExpressableType type) {
		this.impliedType = type;
		// todo : visit whenFragments and otherwise
	}

	@Override
	public ExpressableType getExpressableType() {
		return expressableType;
	}

	@Override
	public ExpressableType getInferableType() {
		if ( otherwise != null ) {
			return otherwise.getInferableType();
		}

		for ( WhenFragment whenFragment : whenFragments ) {
			if ( whenFragment.result.getExpressableType() != null ) {
				return whenFragment.result.getInferableType();
			}
		}

		return expressableType;
	}

	@Override
	public SqmCaseSearched copy(SqmCopyContext context) {
		List<WhenFragment> newWhenFragments = new ArrayList<>( whenFragments.size() );
		for ( WhenFragment fragment : whenFragments ) {
			newWhenFragments.add( new WhenFragment(
				fragment.predicate.copy( context ),
				fragment.result.copy( context )
			));
		}
		return new SqmCaseSearched(
                getSessionFactory(),
				newWhenFragments,
				otherwise.copy( context ),
				expressableType,
				impliedType
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSearchedCaseExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "<searched-case>";
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return expressableType.getJavaTypeDescriptor();
	}

	public static class WhenFragment {
		private final SqmPredicate predicate;
		private final SqmExpression result;

		public WhenFragment(SqmPredicate predicate, SqmExpression result) {
			this.predicate = predicate;
			this.result = result;
		}

		public SqmPredicate getPredicate() {
			return predicate;
		}

		public SqmExpression getResult() {
			return result;
		}
	}
}
