/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.metamodel.model.domain.spi.PersistentAttribute;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.from.SqmFromExporter;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.sql.ast.produce.metamodel.spi.NavigableContainerReferenceInfo;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmAttributeReference<A extends PersistentAttribute>
		extends AbstractSqmNavigableReference
		implements SqmAttributeReference, SqmFromExporter, Path {

	private final SqmNavigableContainerReference sourceReference;
	private final A attribute;
	private final NavigablePath navigablePath;
	private Map<String, Path> attributePathRegistry;

	public AbstractSqmAttributeReference(SqmNavigableContainerReference sourceReference, A attribute, SqmCreationContext creationContext) {
		super( creationContext );
		if ( sourceReference == null ) {
			throw new IllegalArgumentException( "Source for AttributeBinding cannot be null" );
		}
		if ( attribute == null ) {
			throw new IllegalArgumentException( "Attribute for AttributeBinding cannot be null" );
		}

		this.sourceReference = sourceReference;
		this.attribute = attribute;

		this.navigablePath = sourceReference.getNavigablePath().append( attribute.getAttributeName() );
	}

	@Override
	public SqmNavigableContainerReference getSourceReference() {
		// attribute binding must have a source
		return sourceReference;
	}

	@Override
	public Path<?> getParentPath() {
		return (Path<?>) getSourceReference();
	}

	@Override
	public NavigableContainerReferenceInfo getNavigableContainerReferenceInfo() {
		return sourceReference;
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return attribute.getJavaTypeDescriptor();
	}

	@Override
	public Class getJavaType() {
		return getJavaTypeDescriptor().getJavaType();
	}

	@Override
	public A getReferencedNavigable() {
		return attribute;
	}

	@Override
	public Bindable getModel() {
		return (Bindable) getReferencedNavigable();
	}

	protected abstract boolean canBeDereferenced();

	protected final Path resolveCachedAttributePath(String attributeName) {
		return attributePathRegistry == null
				? null
				: attributePathRegistry.get( attributeName );
	}

	protected final void registerAttributePath(String attributeName, Path path) {
		if ( attributePathRegistry == null ) {
			attributePathRegistry = new HashMap<>();
		}
		attributePathRegistry.put( attributeName, path );
	}

	@Override
	public Path get(SingularAttribute attribute) {
		return get( attribute.getName() );
	}

	@Override
	public Expression get(PluralAttribute collection) {
		return get( collection.getName() );
	}

	@Override
	public Expression get(MapAttribute map) {
		return get( map.getName() );
	}

	@Override
	public Path get(String attributeName) {
		if ( !canBeDereferenced() ) {
			throw illegalDereference();
		}

		Path path = resolveCachedAttributePath( attribute.getName() );
		if ( path == null ) {
			path = (Path) resolvePathPart(
					attributeName,
					null,
					false,
					getCreationContext()
			);
			registerAttributePath( attribute.getName(), path );
		}
		return path;
	}

	@Override
	public ExpressableType getExpressableType() {
		return getReferencedNavigable();
	}

	@Override
	public ExpressableType getInferableType() {
		return getExpressableType();
	}

	@Override
	public NavigablePath getNavigablePath() {
		return navigablePath;
	}

	@Override
	public String asLoggableText() {
		return getClass().getSimpleName() + '(' + sourceReference.asLoggableText() + '.' + attribute.getAttributeName() + " : " + getExportedFromElement().getIdentificationVariable() + ")";
	}
	@Override
	public Expression<Class> type() {
		return new SqmEntityTypeExpression( this, getSessionFactory() );
	}
}
