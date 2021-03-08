/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.type;

import java.sql.Date;
import java.time.LocalDate;
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
public class DateArrayTest extends BaseNonConfigCoreFunctionalTestCase {

	@Override
	protected Class[] getAnnotatedClasses() {
		return new Class[]{ TableWithDateArrays.class };
	}

	private LocalDate date1;
	private LocalDate date2;
	private LocalDate date3;
	private LocalDate date4;

	public void startUp() {
		super.startUp();
		inTransaction( em -> {
			// I can't believe anyone ever thought this Date API is a good idea
			date1 = LocalDate.now();
			date2 = date1.plusDays( 5 );
			date3 = date1.plusMonths( 4 );
			date4 = date1.plusYears( 3 );
			em.persist( new TableWithDateArrays( 1L, new LocalDate[]{} ) );
			em.persist( new TableWithDateArrays( 2L, new LocalDate[]{ date1, date2, date3 } ) );
			em.persist( new TableWithDateArrays( 3L, null ) );

			Query q;
			q = em.createNamedQuery( "TableWithDateArrays.Native.insert" );
			q.setParameter( "id", 4L );
			q.setParameter( "data", new LocalDate[]{ null, date4, date2 } );
			q.executeUpdate();

			q = em.createNativeQuery( "INSERT INTO table_with_date_arrays(id, the_array) VALUES ( :id , :data )" );
			q.setParameter( "id", 5L );
			q.setParameter( "data", new LocalDate[]{ null, date4, date2 } );
			q.executeUpdate();
		} );
	}

	@Test
	public void testById() {
		inSession( em -> {
			TableWithDateArrays tableRecord;
			tableRecord = em.find( TableWithDateArrays.class, 1L );
			assertThat( tableRecord.getTheArray(), is( new LocalDate[]{} ) );

			tableRecord = em.find( TableWithDateArrays.class, 2L );
			assertThat( tableRecord.getTheArray(), is( new LocalDate[]{ date1, date2, date3 } ) );

			tableRecord = em.find( TableWithDateArrays.class, 3L );
			assertThat( tableRecord.getTheArray(), is( (Object) null ) );

			tableRecord = em.find( TableWithDateArrays.class, 4L );
			assertThat( tableRecord.getTheArray(), is( new LocalDate[]{ null, date4, date2 } ) );
		} );
	}

	@Test
	public void testQuery() {
		inSession( em -> {
			TableWithDateArrays tableRecord;
			TypedQuery<TableWithDateArrays> tq;

			tq = em.createNamedQuery( "TableWithDateArrays.JPQL.getById", TableWithDateArrays.class );
			tq.setParameter( "id", 2L );
			tableRecord = tq.getSingleResult();
			assertThat( tableRecord.getTheArray(), is( new LocalDate[]{ date1, date2, date3 } ) );

			tq = em.createNamedQuery( "TableWithDateArrays.JPQL.getByData", TableWithDateArrays.class );
			tq.setParameter( "data", new LocalDate[]{} );
			tableRecord = tq.getSingleResult();
			assertThat( tableRecord.getId(), is( 1L ) );
		} );
	}

	@Test
	public void testNativeQuery() {
		inSession( em -> {
			TableWithDateArrays tableRecord;
			TypedQuery<TableWithDateArrays> tq;

			tq = em.createNamedQuery( "TableWithDateArrays.Native.getById", TableWithDateArrays.class );
			tq.setParameter( "id", 2L );
			tableRecord = tq.getSingleResult();
			assertThat( tableRecord.getTheArray(), is( new LocalDate[]{ date1, date2, date3 } ) );

			tq = em.createNamedQuery( "TableWithDateArrays.Native.getByData", TableWithDateArrays.class );
			tq.setParameter( "data", new LocalDate[]{ date1, date2, date3 } );
			tableRecord = tq.getSingleResult();
			assertThat( tableRecord.getId(), is( 2L ) );
		} );
	}

	@Test
	public void testNativeQueryUntyped() {
		inSession( em -> {
			Query q = em.createNamedQuery( "TableWithDateArrays.Native.getByIdUntyped" );
			q.setParameter( "id", 2L );
			Object[] tuple = (Object[]) q.getSingleResult();
			assertThat(
					tuple[1],
					is( new Date[] { Date.valueOf( date1 ), Date.valueOf( date2 ), Date.valueOf( date3 ) } )
			);
		} );
	}

	@Entity( name = "TableWithDateArrays" )
	@Table( name = "table_with_date_arrays" )
	@NamedQueries( {
		@NamedQuery( name = "TableWithDateArrays.JPQL.getById",
				query = "SELECT t FROM TableWithDateArrays t WHERE id = :id" ),
		@NamedQuery( name = "TableWithDateArrays.JPQL.getByData",
				query = "SELECT t FROM TableWithDateArrays t WHERE theArray = :data" ), } )
	@NamedNativeQueries( {
		@NamedNativeQuery( name = "TableWithDateArrays.Native.getById",
				query = "SELECT * FROM table_with_date_arrays t WHERE id = :id",
				resultClass = TableWithDateArrays.class ),
		@NamedNativeQuery( name = "TableWithDateArrays.Native.getByData",
				query = "SELECT * FROM table_with_date_arrays t WHERE the_array = :data",
				resultClass = TableWithDateArrays.class ),
		@NamedNativeQuery( name = "TableWithDateArrays.Native.getByIdUntyped",
				query = "SELECT * FROM table_with_date_arrays t WHERE id = :id" ),
		@NamedNativeQuery( name = "TableWithDateArrays.Native.insert",
				query = "INSERT INTO table_with_date_arrays(id, the_array) VALUES ( :id , :data )" )
	} )
	public static class TableWithDateArrays {

		@Id
		private Long id;

		@Column( name = "the_array" )
		private LocalDate[] theArray;

		public TableWithDateArrays() {
		}

		public TableWithDateArrays(Long id, LocalDate[] theArray) {
			this.id = id;
			this.theArray = theArray;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public LocalDate[] getTheArray() {
			return theArray;
		}

		public void setTheArray(LocalDate[] theArray) {
			this.theArray = theArray;
		}
	}
}
