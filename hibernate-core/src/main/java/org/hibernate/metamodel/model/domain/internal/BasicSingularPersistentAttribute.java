/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import org.hibernate.boot.model.domain.BasicValueMapping;
import org.hibernate.boot.model.domain.PersistentAttributeMapping;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.convert.internal.NamedEnumValueConverter;
import org.hibernate.metamodel.model.convert.internal.OrdinalEnumValueConverter;
import org.hibernate.metamodel.model.convert.internal.StandardBasicValueConverter;
import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.spi.AbstractNonIdSingularPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.BasicValuedNavigable;
import org.hibernate.metamodel.model.domain.spi.ConvertibleNavigable;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.NavigableVisitationStrategy;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableContainerReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReferenceBasic;
import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.results.internal.ScalarQueryResultImpl;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.type.descriptor.java.internal.EnumJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.spi.ValueBinder;
import org.hibernate.type.descriptor.spi.ValueExtractor;
import org.hibernate.type.spi.BasicType;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class BasicSingularPersistentAttribute<O, J>
		extends AbstractNonIdSingularPersistentAttribute<O, J>
		implements BasicValuedNavigable<J>, ConvertibleNavigable<J> {
	private static final Logger log = Logger.getLogger( BasicSingularPersistentAttribute.class );

	private final Column boundColumn;
	private final BasicType<J> basicType;
	private final BasicValueConverter valueConverter;

	@SuppressWarnings("unchecked")
	public BasicSingularPersistentAttribute(
			ManagedTypeDescriptor<O> runtimeContainer,
			PersistentAttributeMapping bootAttribute,
			PropertyAccess propertyAccess,
			Disposition disposition,
			RuntimeModelCreationContext context) {
		super(
				runtimeContainer,
				bootAttribute,
				propertyAccess,
				disposition
		);

		final BasicValueMapping<J> basicValueMapping = (BasicValueMapping<J>) bootAttribute.getValueMapping();
		this.boundColumn = context.getDatabaseObjectResolver().resolveColumn( basicValueMapping.getMappedColumn() );
		this.basicType = basicValueMapping.resolveType();

		this.valueConverter = basicValueMapping.getAttributeConverterDescriptor().createJpaAttributeConverter( context );

		if ( valueConverter != null ) {
			log.debugf(
					"BasicValueConverter [%s] being applied for basic attribute : %s",
					valueConverter,
					getNavigableRole()
			);
		}
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.BASIC;
	}

	@Override
	public BasicValuedExpressableType<J> getType() {
		return (BasicValuedExpressableType<J>) super.getType();
	}

	@Override
	public BasicJavaDescriptor<J> getJavaTypeDescriptor() {
		return (BasicJavaDescriptor<J>) super.getJavaTypeDescriptor();
	}

	@Override
	public SqmNavigableReference createSqmExpression(
			SqmFrom sourceSqmFrom,
			SqmNavigableContainerReference containerReference,
			SqmCreationContext creationContext) {
		return new SqmSingularAttributeReferenceBasic( containerReference, this, creationContext );
	}

	@Override
	public QueryResult createQueryResult(
			NavigableReference navigableReference,
			String resultVariable,
			QueryResultCreationContext creationContext) {
		return new ScalarQueryResultImpl(
				resultVariable,
				creationContext.getSqlSelectionResolver().resolveSqlSelection(
						creationContext.getSqlSelectionResolver().resolveSqlExpression(
								navigableReference.getSqlExpressionQualifier(),
								boundColumn
						)
				),
				getType()
		);
	}

	@Override
	public Column getBoundColumn() {
		return boundColumn;
	}

	@Override
	public String asLoggableText() {
		return "SingularAttributeBasic(" + getContainer().asLoggableText() + '.' + getAttributeName() + ')';
	}

	@Override
	public BasicValueConverter getValueConverter() {
		return valueConverter;
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return PersistentAttributeType.BASIC;
	}

	@Override
	public void visitNavigable(NavigableVisitationStrategy visitor) {
		visitor.visitSingularAttributeBasic( this );
	}

	@Override
	public BasicType<J> getBasicType() {
		return basicType;
	}

	@Override
	public ValueBinder getValueBinder() {
		return basicType.getValueBinder();
	}

	@Override
	public ValueExtractor getValueExtractor() {
		return basicType.getValueExtractor();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object resolveHydratedState(
			Object hydratedForm,
			SharedSessionContractImplementor session,
			Object containerInstance) {
		return valueConverter.toDomainValue( hydratedForm, session );
	}

}
