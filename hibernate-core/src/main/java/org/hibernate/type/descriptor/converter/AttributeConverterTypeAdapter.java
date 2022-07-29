/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.converter;

import org.hibernate.boot.model.convert.spi.ConverterDescriptor;
import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.metamodel.model.convert.spi.JpaAttributeConverter;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.java.MutabilityPlan;

import org.jboss.logging.Logger;

/**
 * Adapts the Hibernate Type contract to incorporate JPA AttributeConverter calls.
 *
 * @author Steve Ebersole
 */
public class AttributeConverterTypeAdapter<T> extends AbstractSingleColumnStandardBasicType<T> {
	private static final Logger log = Logger.getLogger( AttributeConverterTypeAdapter.class );

	@SuppressWarnings("unused")
	public static final String NAME_PREFIX = ConverterDescriptor.TYPE_NAME_PREFIX;

	private final String name;
	private final String description;

	private final JavaType<T> domainJtd;
	private final JavaType<?> relationalJtd;
	private final BasicValueConverter<T, Object> attributeConverter;

	private final MutabilityPlan<T> mutabilityPlan;

	public AttributeConverterTypeAdapter(
			String name,
			String description,
			BasicValueConverter<? extends T, ?> attributeConverter,
			AttributeConverterJdbcTypeAdapter jdbcType,
			JavaType<?> relationalJtd,
			JavaType<T> domainJtd,
			MutabilityPlan<T> mutabilityPlan) {
		super( jdbcType, domainJtd );
		this.name = name;
		this.description = description;
		this.domainJtd = domainJtd;
		this.relationalJtd = relationalJtd;
		this.attributeConverter = (BasicValueConverter<T, Object>) attributeConverter;

		// NOTE : the way that JpaAttributeConverter get built, their "domain JTD" already
		// contains the proper MutabilityPlan based on whether the `@Immutable` is present
		if ( mutabilityPlan == null ) {
			this.mutabilityPlan = (MutabilityPlan<T>) attributeConverter.getDomainJavaType().getMutabilityPlan();
		}
		else {
			this.mutabilityPlan = mutabilityPlan;
		}

		log.debugf( "Created AttributeConverterTypeAdapter -> %s", name );
	}

	@Override
	public String getName() {
		return name;
	}

	public JavaType<T> getDomainJtd() {
		return domainJtd;
	}

	public JavaType<?> getRelationalJtd() {
		return relationalJtd;
	}

	public JpaAttributeConverter<? extends T, ?> getAttributeConverter() {
		return (JpaAttributeConverter<? extends T, ?>) attributeConverter;
	}

	@Override
	public BasicValueConverter<T, ?> getValueConverter() {
		return attributeConverter;
	}

	@Override
	protected MutabilityPlan<T> getMutabilityPlan() {
		return mutabilityPlan;
	}

	@Override
	public String toString() {
		return description;
	}

}
