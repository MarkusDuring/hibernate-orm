/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.criteria.spi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import javax.persistence.criteria.Expression;

import org.hibernate.Incubating;
import org.hibernate.query.criteria.JpaInImplementor;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * Hibernate ORM specialization of the JPA {@link Expression} contract.
 *
 * @author Steve Ebersole
 *
 * @since 6.0
 */
@Incubating
public interface JpaExpressionImplementor<T> extends JpaSelectionImplementor<T>, Expression<T> {

	SqmExpression getSqmExpression();

	<X> JpaExpressionImplementor<X> as(JavaTypeDescriptor<X> type);

	/**
	 * Specialized form of {@link #as} for treating as a Long
	 */
	default JpaExpressionImplementor<Long> asLong() {
		return as(
				getCriteriaBuilder().getSessionFactory()
						.getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( Long.class )
		);
	}

	default JpaExpressionImplementor<Integer> asInteger() {
		return as(
				getCriteriaBuilder().getSessionFactory()
						.getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( Integer.class )
		);
	}

	default JpaExpressionImplementor<Float> asFloat() {
		return as(
				getCriteriaBuilder().getSessionFactory()
						.getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( Float.class )
		);
	}

	default JpaExpressionImplementor<Double> asDouble() {
		return as(
				getCriteriaBuilder().getSessionFactory()
						.getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( Double.class )
		);
	}

	default JpaExpressionImplementor<BigDecimal> asBigDecimal() {
		return as(
				getCriteriaBuilder().getSessionFactory()
						.getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( BigDecimal.class )
		);
	}

	default JpaExpressionImplementor<BigInteger> asBigInteger() {
		return as(
				getCriteriaBuilder().getSessionFactory()
						.getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( BigInteger.class )
		);
	}

	default JpaExpressionImplementor<String> asString() {
		return as(
				getCriteriaBuilder().getSessionFactory()
						.getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( String.class )
		);
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// covariant returns

	default <X> JpaExpressionImplementor<X> as(Class<X> type) {
		return as(
				getCriteriaBuilder().getSessionFactory()
						.getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( type )
		);
	}

	default JpaPredicateImplementor isNull() {
		return getCriteriaBuilder().isNull( this );
	}

	default JpaPredicateImplementor isNotNull() {
		return getCriteriaBuilder().isNotNull( this );
	}

	default JpaPredicateImplementor in(Object... values) {
		return getCriteriaBuilder().in( this, values );
	}

	default JpaPredicateImplementor in(Expression<?>... values) {
		return getCriteriaBuilder().in( this, values );
	}

	default JpaPredicateImplementor in(Collection<?> values) {
		return getCriteriaBuilder().in( this, values.toArray() );
	}

	default JpaPredicateImplementor in(Expression<Collection<?>> values) {
		return getCriteriaBuilder().in( this, values );
	}

	JpaSelectionImplementor<T> alias(String name);
}
