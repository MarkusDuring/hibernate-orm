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
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.hibernate.test.bytecode.enhancement.AbstractEnhancerTestTask;
import org.hibernate.test.bytecode.enhancement.merge.RefreshEnhancedEntityTestTask;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OneToOneLazyLoadingTestTask extends AbstractEnhancerTestTask {

	private SQLStatementInspector statementInspector = new SQLStatementInspector();

	@Override
	public Class<?>[] getAnnotatedClasses() {
		return new Class[] { Parent.class, Child.class };
	}

	@Override
	public void prepare() {
		prepare(new Configuration());
	}

	@Override
	protected void addSettings(Map settings) {
		super.addSettings( settings );
		settings.put( Environment.STATEMENT_INSPECTOR, statementInspector );
	}

	public void execute() {
		inTransaction(
				session -> {
					Child person = new Child( new PK( 1, 1 ) );
					Parent testEntity = new Parent( 1, "test", person );
					session.persist( testEntity );
				}
		);
		inTransaction(
				session -> {
					Child person = new Child( new PK( 1, 1 ) );
					person.name = "abc";
					session.merge( person );

//					statementInspector.clear();
//					session.get( Child.class ,new PK( 1, 1 ) );
//					String query = statementInspector.getSqlQueries().get( 0 );
//					assertEquals( 1, getNumberOfJoins( query ) );
//					statementInspector.clear();
				}
		);
	}

	protected void cleanup() {
	}

	private void inTransaction(Consumer<Session> action) {
		final Session s = getFactory().openSession();
		Transaction transaction = s.beginTransaction();
		try {
			action.accept( s );
			transaction.commit();
		}
		catch (Exception ex) {
			transaction.rollback();
		}
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

//		@OneToOne(cascade = CascadeType.ALL)
		@OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL,  mappedBy = "parent",optional = false)
		@LazyGroup("claimantPartyEntities")
		@LazyToOne(LazyToOneOption.NO_PROXY)
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

		@OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL, optional = false)
		@NotFound(action= NotFoundAction.IGNORE)
		@LazyToOne(LazyToOneOption.PROXY)
		private Parent parent;


		public Child() {
		}

		public Child(PK id) {
			this.id = id;
		}

		public Child(PK id, String name, Parent parent) {
			this.id = id;
			this.name = name;
			this.parent = parent;
		}
	}


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
