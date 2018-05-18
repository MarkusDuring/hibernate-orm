/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.criteria.internal.PathImplementor;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.sql.ast.produce.metamodel.spi.EntityValuedExpressableType;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * @author Steve Ebersole
 */
public interface SqmEntityTypedReference extends SqmNavigableContainerReference, PathImplementor {
	@Override
	SqmEntityTypedReference copy(SqmCopyContext context);

	@Override
	EntityValuedExpressableType getReferencedNavigable();

	@Override
	EntityValuedExpressableType getExpressableType();

	@Override
	default JavaTypeDescriptor getJavaTypeDescriptor() {
		return getExpressableType().getJavaTypeDescriptor();
	}

	@Override
	default Class getJavaType() {
		return getJavaTypeDescriptor().getJavaType();
	}

	@Override
	default EntityValuedExpressableType getInferableType() {
		return getExpressableType();
	}

	@Override
	default Bindable getModel() {
		return (Bindable) getReferencedNavigable();
	}

	@Override
	default Path<?> getParentPath() {
		return (Path<?>) getSourceReference();
	}

	@Override
	default Path get(SingularAttribute attribute) {
		return get( attribute.getName() );
	}

	@Override
	default Expression get(PluralAttribute collection) {
		return (Expression) get( collection.getName() );
	}

	@Override
	default Expression get(MapAttribute map) {
		return (Expression) get( map.getName() );
	}

	@Override
	default Path get(String attributeName) {
		return (Path) resolvePathPart( attributeName, null, false, getCreationContext() );
	}

	@Override
	default Expression<Class> type() {
		return new SqmEntityTypeExpression(this, getSessionFactory());
	}
}
