/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.named.spi.ParameterMemento;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class SqmAnonymousParameter extends AbstractSqmExpression implements SqmParameter, QueryParameterImplementor {
	private final boolean canBeMultiValued;
	private final Class<?> javaType;
	private ExpressableType expressableType;

	public SqmAnonymousParameter(SessionFactoryImplementor sessionFactory, boolean canBeMultiValued) {
		super( sessionFactory );
		this.canBeMultiValued = canBeMultiValued;
		this.javaType = null;
	}

	public SqmAnonymousParameter(SessionFactoryImplementor sessionFactory, boolean canBeMultiValued, Class<?> javaType) {
		super( sessionFactory );
		this.canBeMultiValued = canBeMultiValued;
		this.javaType = javaType;
	}

	private SqmAnonymousParameter(SessionFactoryImplementor sessionFactory, boolean canBeMultiValued, Class<?> javaType, ExpressableType expressableType) {
		super( sessionFactory );
		this.canBeMultiValued = canBeMultiValued;
		this.javaType = javaType;
		this.expressableType = expressableType;
	}

	@Override
	public ExpressableType getExpressableType() {
		return expressableType;
	}

	@Override
	public ExpressableType getInferableType() {
		return getExpressableType();
	}

	@Override
	public void impliedType(ExpressableType expressableType) {
		if ( expressableType != null ) {
			this.expressableType = expressableType;
		}
	}

	@Override
	public SqmAnonymousParameter copy(SqmCopyContext context) {
		// Since these parameters are identified by their identity, we need the same object here
		// Maybe at some point, we could try to assign an id to it and use that instead?
		// todo : figure out how to do this properly when introducing the TypeContext stuff we talked about
		return this;
//		return new SqmAnonymousParameter(
//                getSessionFactory(),
//				canBeMultiValued,
//				javaType,
//				expressableType
//		);
	}

	@Override
	public ParameterMemento toMemento() {
		return (session) -> this;
	}

	@Override
	public Class getParameterType() {
		if ( javaType != null ) {
			return javaType;
		}
		return getJavaTypeDescriptor().getJavaType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitAnonymousParameterExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "<param:anonymous>";
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Integer getPosition() {
		return null;
	}

	@Override
	public boolean allowMultiValuedBinding() {
		return canBeMultiValued;
	}

	@Override
	public ExpressableType getAnticipatedType() {
		return getExpressableType();
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return expressableType.getJavaTypeDescriptor();
	}

	@Override
	public boolean allowsMultiValuedBinding() {
		return canBeMultiValued;
	}

	@Override
	public AllowableParameterType getHibernateType() {
		return (AllowableParameterType) getExpressableType();

	}
}
