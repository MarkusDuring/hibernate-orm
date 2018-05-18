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
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Ebersole
 */
public class SqmCaseSimple extends AbstractSqmExpression implements ImpliedTypeSqmExpression, CriteriaBuilder.SimpleCase {
	private final SqmExpression fixture;
	private List<WhenFragment> whenFragments = new ArrayList<>();
	private SqmExpression otherwise;

	private ExpressableType expressableType;
	private ExpressableType impliedType;

	public SqmCaseSimple(SessionFactoryImplementor sessionFactory, SqmExpression fixture) {
		super( sessionFactory );
		this.fixture = fixture;
	}

	private SqmCaseSimple(
			SessionFactoryImplementor sessionFactory,
			SqmExpression fixture,
			List<WhenFragment> whenFragments,
			SqmExpression otherwise,
			ExpressableType expressableType,
			ExpressableType impliedType) {
		super( sessionFactory );
		this.fixture = fixture;
		this.whenFragments = whenFragments;
		this.otherwise = otherwise;
		this.expressableType = expressableType;
		this.impliedType = impliedType;
	}

	public SqmExpression getFixture() {
		return fixture;
	}

	public List<WhenFragment> getWhenFragments() {
		return whenFragments;
	}

	public SqmExpression getOtherwise() {
		return otherwise;
	}

	public void otherwise(SqmExpression otherwiseExpression) {
		this.otherwise = otherwiseExpression;
		// todo : inject implied expressableType?
	}

	public void when(SqmExpression test, SqmExpression result) {
		whenFragments.add( new WhenFragment( test, result ) );
		// todo : inject implied expressableType?
	}

	@Override
	public void impliedType(ExpressableType type) {
		this.impliedType = type;
		// todo : visit whenFragments and elseExpression
	}

	@Override
	public Expression getExpression() {
		return fixture;
	}

	@Override
	public CriteriaBuilder.SimpleCase when(Object condition, Object result) {
		when( (SqmExpression) getCriteriaBuilder().literal( condition ), (SqmExpression) getCriteriaBuilder().literal( result ) );
		return this;
	}

	@Override
	public CriteriaBuilder.SimpleCase when(Object condition, Expression result) {
		when( (SqmExpression) getCriteriaBuilder().literal( condition ), (SqmExpression) result );
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
	public SqmCaseSimple copy(SqmCopyContext context) {
		List<WhenFragment> newWhenFragments = new ArrayList<>( whenFragments.size() );
		for ( WhenFragment fragment : whenFragments ) {
			newWhenFragments.add( new WhenFragment(
					fragment.checkValue.copy( context ),
					fragment.result.copy( context )
			));
		}
		return new SqmCaseSimple(
                getSessionFactory(),
				fixture.copy( context ),
				newWhenFragments,
				otherwise.copy( context ),
				expressableType,
				impliedType
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSimpleCaseExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "<simple-case>";
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getExpressableType().getJavaTypeDescriptor();
	}

	public static class WhenFragment {
		private final SqmExpression checkValue;
		private final SqmExpression result;

		public WhenFragment(SqmExpression checkValue, SqmExpression result) {
			this.checkValue = checkValue;
			this.result = result;
		}

		public SqmExpression getCheckValue() {
			return checkValue;
		}

		public SqmExpression getResult() {
			return result;
		}
	}
}
