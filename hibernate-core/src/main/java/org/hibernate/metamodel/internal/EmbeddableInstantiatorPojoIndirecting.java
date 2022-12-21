/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.internal;

import java.lang.reflect.Constructor;

import org.hibernate.InstantiationException;
import org.hibernate.bytecode.spi.BytecodeProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.spi.ValueAccess;

/**
 * Support for instantiating embeddables as POJO representation through a constructor
 */
public class EmbeddableInstantiatorPojoIndirecting extends AbstractPojoInstantiator implements StandardEmbeddableInstantiator {
	protected final Constructor<?> constructor;
	protected final int[] index;

	protected EmbeddableInstantiatorPojoIndirecting(Constructor<?> constructor, int[] index) {
		super( constructor.getDeclaringClass() );
		this.constructor = constructor;
		this.index = index;
	}

	public static EmbeddableInstantiatorPojoIndirecting of(
			String[] propertyNames,
			Constructor<?> constructor,
			BytecodeProvider bytecodeProvider) {
		final String[] componentNames = bytecodeProvider.determineConstructorArgumentFieldAssignments( constructor );
		if ( componentNames == null ) {
			throw new IllegalArgumentException( "Can't determine field assignment for constructor: " + constructor );
		}
		final int[] index = new int[componentNames.length];
		if ( EmbeddableHelper.resolveIndex( propertyNames, componentNames, index ) ) {
			return new EmbeddableInstantiatorPojoIndirecting.EmbeddableInstantiatorPojoIndirectingWithGap( constructor, index );
		}
		else {
			return new EmbeddableInstantiatorPojoIndirecting( constructor, index );
		}
	}

	@Override
	public Object instantiate(ValueAccess valuesAccess, SessionFactoryImplementor sessionFactory) {
		try {
			final Object[] originalValues = valuesAccess.getValues();
			final Object[] values = new Object[originalValues.length];
			for ( int i = 0; i < values.length; i++ ) {
				values[i] = originalValues[index[i]];
			}
			return constructor.newInstance( values );
		}
		catch ( Exception e ) {
			throw new InstantiationException( "Could not instantiate entity: ", getMappedPojoClass(), e );
		}
	}

	// Handles gaps, by leaving the value null for that index
	private static class EmbeddableInstantiatorPojoIndirectingWithGap extends EmbeddableInstantiatorPojoIndirecting {

		public EmbeddableInstantiatorPojoIndirectingWithGap(Constructor<?> constructor, int[] index) {
			super( constructor, index );
		}

		@Override
		public Object instantiate(ValueAccess valuesAccess, SessionFactoryImplementor sessionFactory) {
			try {
				final Object[] originalValues = valuesAccess.getValues();
				final Object[] values = new Object[index.length];
				for ( int i = 0; i < values.length; i++ ) {
					final int index = this.index[i];
					if ( index >= 0 ) {
						values[i] = originalValues[index];
					}
				}
				return constructor.newInstance( values );
			}
			catch ( Exception e ) {
				throw new InstantiationException( "Could not instantiate entity: ", getMappedPojoClass(), e );
			}
		}
	}
}
