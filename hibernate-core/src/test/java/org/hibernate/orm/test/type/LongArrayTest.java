/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import org.hibernate.testing.DialectChecks;
import org.hibernate.testing.RequiresDialectFeature;
import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jordan Gigov
 * @author Christian Beikov
 */
@RequiresDialectFeature(DialectChecks.SupportsArrayDataTypes.class)
public class LongArrayTest extends BaseNonConfigCoreFunctionalTestCase {

	@Override
	protected Class[] getAnnotatedClasses() {
		return new Class[]{ TableWithLongArrays.class };
	}

	public void startUp() {
		super.startUp();
		inTransaction( em -> {
			em.persist( new TableWithLongArrays( 1L, new Long[]{} ) );

			em.persist( new TableWithLongArrays( 2L, new Long[]{ 4L, 8L, 15L, 16L, 23L, 42L } ) );

			em.persist( new TableWithLongArrays( 3L, null ) );

			Query q;
			q = em.createNamedQuery( "TableWithLongArrays.Native.insert" );
			q.setParameter( "id", 4L );
			q.setParameter( "data", new Long[]{ 4L, 8L, 15L, 16L, null, 23L, 42L } );
			q.executeUpdate();

			q = em.createNativeQuery( "INSERT INTO table_with_bigint_arrays(id, the_array) VALUES ( :id , :data )" );
			q.setParameter( "id", 5L );
			q.setParameter( "data", new Long[]{ 4L, 8L, null, 16L, null, 23L, 42L } );
			q.executeUpdate();
		} );
	}

	@Test
	public void testById() {
		inSession( em -> {
			TableWithLongArrays tableRecord;
			tableRecord = em.find( TableWithLongArrays.class, 1L );
			assertThat( tableRecord.getTheArray(), is( new Long[]{} ) );

			tableRecord = em.find( TableWithLongArrays.class, 2L );
			assertThat( tableRecord.getTheArray(), is( new Long[]{ 4L, 8L, 15L, 16L, 23L, 42L } ) );

			tableRecord = em.find( TableWithLongArrays.class, 4L );
			assertThat( tableRecord.getTheArray(), is( new Long[]{ 4L, 8L, 15L, 16L, null, 23L, 42L } ) );

			tableRecord = em.find( TableWithLongArrays.class, 5L );
			assertThat( tableRecord.getTheArray(), is( new Long[]{ 4L, 8L, null, 16L, null, 23L, 42L } ) );
		} );
	}

	@Test
	public void testQuery() {
		inSession( em -> {
			TableWithLongArrays tableRecord;
			TypedQuery<TableWithLongArrays> tq;

			tq = em.createNamedQuery( "TableWithLongArrays.JPQL.getById", TableWithLongArrays.class );
			tq.setParameter( "id", 2L );
			tableRecord = tq.getSingleResult();
			assertThat( tableRecord.getTheArray(), is( new Long[]{ 4L, 8L, 15L, 16L, 23L, 42L } ) );

			tq = em.createNamedQuery( "TableWithLongArrays.JPQL.getByData", TableWithLongArrays.class );
			tq.setParameter( "data", new Long[]{ 4L, 8L, 15L, 16L, null, 23L, 42L } );
			tableRecord = tq.getSingleResult();
			assertThat( tableRecord.getId(), is( 4L ) );
		} );
	}

	@Test
	public void testNativeQuery() {
		inSession( em -> {
			TableWithLongArrays tableRecord;
			TypedQuery<TableWithLongArrays> tq;

			tq = em.createNamedQuery( "TableWithLongArrays.Native.getById", TableWithLongArrays.class );
			tq.setParameter( "id", 2L );
			tableRecord = tq.getSingleResult();
			assertThat( tableRecord.getTheArray(), is( new Long[]{ 4L, 8L, 15L, 16L, 23L, 42L } ) );

			tq = em.createNamedQuery( "TableWithLongArrays.Native.getByData", TableWithLongArrays.class );
			tq.setParameter( "data", new Long[]{ 4L, 8L, 15L, 16L, null, 23L, 42L } );
			tableRecord = tq.getSingleResult();
			assertThat( tableRecord.getId(), is( 4L ) );
		} );
	}

	@Test
	public void testNativeQueryUntyped() {
		inSession( em -> {
			Query q = em.createNamedQuery( "TableWithLongArrays.Native.getByIdUntyped" );
			q.setParameter( "id", 2L );
			Object[] tuple = (Object[]) q.getSingleResult();
			assertThat( tuple[1], is( new Long[]{ 4L, 8L, 15L, 16L, 23L, 42L } ) );
		} );
	}

	@Entity( name = "TableWithLongArrays" )
	@Table( name = "table_with_bigint_arrays" )
	@NamedQueries( {
		@NamedQuery( name = "TableWithLongArrays.JPQL.getById",
				query = "SELECT t FROM TableWithLongArrays t WHERE id = :id" ),
		@NamedQuery( name = "TableWithLongArrays.JPQL.getByData",
				query = "SELECT t FROM TableWithLongArrays t WHERE theArray = :data" ), } )
	@NamedNativeQueries( {
		@NamedNativeQuery( name = "TableWithLongArrays.Native.getById",
				query = "SELECT * FROM table_with_bigint_arrays t WHERE id = :id",
				resultClass = TableWithLongArrays.class ),
		@NamedNativeQuery( name = "TableWithLongArrays.Native.getByData",
				query = "SELECT * FROM table_with_bigint_arrays t WHERE the_array = :data",
				resultClass = TableWithLongArrays.class ),
		@NamedNativeQuery( name = "TableWithLongArrays.Native.getByIdUntyped",
				query = "SELECT * FROM table_with_bigint_arrays t WHERE id = :id" ),
		@NamedNativeQuery( name = "TableWithLongArrays.Native.insert",
				query = "INSERT INTO table_with_bigint_arrays(id, the_array) VALUES ( :id , :data )" )
	} )
	public static class TableWithLongArrays {

		@Id
		private Long id;

		@Column( name = "the_array" )
		private Long[] theArray;

		public TableWithLongArrays() {
		}

		public TableWithLongArrays(Long id, Long[] theArray) {
			this.id = id;
			this.theArray = theArray;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long[] getTheArray() {
			return theArray;
		}

		public void setTheArray(Long[] theArray) {
			this.theArray = theArray;
		}
	}
}
