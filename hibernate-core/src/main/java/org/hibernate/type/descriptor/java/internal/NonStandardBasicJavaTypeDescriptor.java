/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.descriptor.java.internal;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.tree.expression.SqmLiteral;
import org.hibernate.type.descriptor.java.spi.AbstractBasicJavaDescriptor;
import org.hibernate.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.type.descriptor.spi.WrapperOptions;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.type.spi.BasicType;

/**
 * @author Steve Ebersole
 */
public class NonStandardBasicJavaTypeDescriptor<T>
		extends AbstractBasicJavaDescriptor<T> {
	public NonStandardBasicJavaTypeDescriptor(Class<T> type) {
		super( type );
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		// none
		return null;
	}

	@Override
	public SqmLiteral<T> createLiteralExpression(SessionFactoryImplementor sessionFactory, BasicType<T> basicType, T value) {
		return null;
	}

	@Override
	public String toString(T value) {
		return null;
	}

	@Override
	public T fromString(String string) {
		return null;
	}

	@Override
	public <X> X unwrap(T value, Class<X> type, WrapperOptions options) {
		return null;
	}

	@Override
	public <X> T wrap(X value, WrapperOptions options) {
		return null;
	}

	public String asLoggableText() {
		return "{non-standard-basic-type(" + getJavaType().getName() + "}";
	}

}
