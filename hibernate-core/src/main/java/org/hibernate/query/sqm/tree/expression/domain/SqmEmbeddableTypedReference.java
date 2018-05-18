/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.metamodel.model.domain.spi.EmbeddedValuedNavigable;
import org.hibernate.query.criteria.internal.PathImplementor;
import org.hibernate.query.sqm.tree.from.SqmFromExporter;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * @author Steve Ebersole
 */
public interface SqmEmbeddableTypedReference extends SqmNavigableContainerReference, SqmFromExporter, PathImplementor {
	@Override
	EmbeddedValuedNavigable getReferencedNavigable();

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
		return (Expression) resolvePathPart( collection.getName(), null, false, getCreationContext() );
	}

	@Override
	default Expression get(MapAttribute map) {
		return (Expression) resolvePathPart( map.getName(), null, false, getCreationContext() );
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
