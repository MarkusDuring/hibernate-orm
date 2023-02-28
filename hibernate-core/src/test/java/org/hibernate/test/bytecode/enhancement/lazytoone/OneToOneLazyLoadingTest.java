package org.hibernate.test.bytecode.enhancement.lazytoone;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.LazyGroup;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.cfg.Environment;

import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

//@RunWith(BytecodeEnhancerRunner.class)
public class OneToOneLazyLoadingTest extends BaseNonConfigCoreFunctionalTestCase {

	private SQLStatementInspector statementInspector = new SQLStatementInspector();

	@Override
	protected Class[] getAnnotatedClasses() {
		return new Class[] { Parent.class, Child.class, Address.class };
	}
	@Override
	protected void addSettings(Map settings) {
		settings.put( Environment.STATEMENT_INSPECTOR, statementInspector );
	}

	@Test
	public void testMerge() {
		statementInspector.clear();
		inTransaction(
				sesssion -> {
					Child person = new Child( new PK( 1, 1 ) );

					Parent testEntity = new Parent( 1, "test", person );
					sesssion.merge( testEntity );
				}
		);
		String query = statementInspector.getSqlQueries().get( 0 );
		assertEquals( 1, getNumberOfJoins( query ) );
		statementInspector.clear();
		inTransaction(
				sesssion -> {
					sesssion.get( Child.class ,new PK( 1, 1 ) );
				}
		);

	}

	private void inTransaction(Consumer<Session> action) {
		final Session s = sessionFactory().openSession();
		Transaction transaction = s.beginTransaction();
		try {
			action.accept( s );
			transaction.commit();
		}
		catch (Exception ex) {
			transaction.rollback();
		}
	}

	@Test
	public void testMerge2() {
		statementInspector.clear();
		inTransaction(
				sesssion -> {
					Child person = new Child( new PK( 1, 1 ) );

//                  Parent testEntity = new Parent( 1, "test", person );
					sesssion.merge( person );
				}
		);
		String query = statementInspector.getSqlQueries().get( 0 );
		assertEquals( 0, getNumberOfJoins( query ) );
		statementInspector.clear();
//      inTransaction(
//              sesssion -> {
//                  sesssion.find( Child.class ,new PK( 1, 1 ) );
//              }
//      );

	}

	private static int getNumberOfJoins(String sql) {
		String fromPart = sql.toLowerCase( Locale.ROOT ).split( " from " )[1].split( " where " )[0];
		return fromPart.split( "(\\sjoin\\s|,\\s)", -1 ).length - 1;
	}

	@Entity(name = "Parent")
	public static class Parent {
		@Id
		private Integer id;

		private String name;

		@OneToOne(cascade = CascadeType.ALL)
		private Child child;

		public Parent() {
		}

		public Parent(Integer id, String name, Child child) {
			this.id = id;
			this.name = name;
			this.child = child;
		}
	}

	@Entity(name = "Child")
	@Table(name = "COMMON_TABLE")
	public static class Child {
		@EmbeddedId
		protected PK id;

		private String name;

		@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
		@LazyGroup("address")
		@LazyToOne(LazyToOneOption.PROXY)
		@JoinColumns({
				@JoinColumn(name = "PK_1", referencedColumnName = "PK_1"),
				@JoinColumn(name = "PK_2", referencedColumnName = "PK_2")
		})
		private Address address;

		public Child() {
		}

		public Child(PK id) {
			this.id = id;
		}

		public Child(PK id, Address address) {
			this.id = id;
			this.address = address;
//          this.job = job;
		}
	}

	@Entity(name = "Address")
	@Table(name = "COMMON_TABLE_2")
	public static class Address {
		@EmbeddedId
		private PK id;

		private String description;

		public Address() {
		}

		public Address(PK id, String name) {
			this.id = id;
			this.description = name;
		}
	}

//  @Entity(name = "Job")
//  @Table(name = "COMMON_TABLE")
//  public static class Job {
//      @EmbeddedId
//      private PK id;
//
//      private String description;
//
//      public Job() {
//      }
//
//      public Job(PK id, String description) {
//          this.id = id;
//          this.description = description;
//      }
//  }


	@Embeddable
	public static class PK implements Serializable {
		@Column(name = "PK_1")
		Integer pk1;

		@Column(name = "PK_2")
		Integer pk2;


		public PK() {
		}

		public PK(Integer pk1, Integer pk2) {
			this.pk1 = pk1;
			this.pk2 = pk2;
		}
	}
}
