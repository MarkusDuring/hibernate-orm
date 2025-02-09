/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.softdelete;

import java.sql.Statement;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SoftDelete;

import org.hibernate.testing.jdbc.SQLStatementInspector;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
@DomainModel( annotatedClasses = { ToOneTests.Issue.class, ToOneTests.User.class } )
@SessionFactory(useCollectingStatementInspector = true)
public class ToOneTests {
	@BeforeEach
	void createTestData(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			final User steve = new User( 1, "Steve" );
			final User john = new User( 2, "John" );
			final User jacob = new User( 3, "Jacob" );
			final User bobby = new User( 4, "Bobby" );
			session.persist( steve );
			session.persist( john );
			session.persist( jacob );
			session.persist( bobby );

			final Issue first = new Issue( 1, "first", jacob, steve );
			final Issue second = new Issue( 2, "second", bobby, steve );
			final Issue third = new Issue( 3, "third", jacob, john );
			session.persist( first );
			session.persist( second );
			session.persist( third );

			// soft-delete John and Bobby
			session.createMutationQuery( "delete User where id in (2,4)" ).executeUpdate();
		} );
	}

	@AfterEach
	void dropTestData(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> session.doWork( (connection) -> {
			final Statement statement = connection.createStatement();
			statement.execute( "delete from issues" );
			statement.execute( "delete from users" );
		} ) );
	}

	@Test
	void basicBaselineTest(SessionFactoryScope scope) {
		final SQLStatementInspector sqlInspector = scope.getCollectingStatementInspector();
		sqlInspector.clear();

		scope.inTransaction( (session) -> {
			final Issue issue1 = session.get( Issue.class, 1 );
			assertThat( issue1 ).isNotNull();
			assertThat( issue1.reporter ).isNotNull();
			assertThat( issue1.assignee ).isNotNull();

			assertThat( sqlInspector.getSqlQueries() ).hasSize( 2 );
			assertThat( sqlInspector.getSqlQueries().get( 0 ) ).contains( " join " );
			assertThat( sqlInspector.getSqlQueries().get( 0 ) ).contains( ".reporter_fk" );
			assertThat( sqlInspector.getSqlQueries().get( 0 ) ).contains( ".active='Y" );
			assertThat( sqlInspector.getSqlQueries().get( 0 ) ).containsOnlyOnce( "active" );
			assertThat( sqlInspector.getSqlQueries().get( 1 ) ).doesNotContain( " join " );
			assertThat( sqlInspector.getSqlQueries().get( 1 ) ).contains( ".active='Y" );
			assertThat( sqlInspector.getSqlQueries().get( 1 ) ).containsOnlyOnce( "active" );
		} );
	}

	@Test
	void basicJoinedTest(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			final Issue issue2 = session.get( Issue.class, 2 );
			assertThat( issue2 ).isNotNull();
			assertThat( issue2.reporter ).isNull();
			assertThat( issue2.assignee ).isNotNull();
		} );
	}

	@Test
	void basicSelectedTest(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			final Issue issue3 = session.get( Issue.class, 3 );
			assertThat( issue3 ).isNotNull();
			assertThat( issue3.reporter ).isNotNull();
			assertThat( issue3.assignee ).isNull();
		} );
	}

	@Entity(name="Issue")
	@Table(name="issues")
	public static class Issue {
		@Id
		private Integer id;
		private String description;
		@ManyToOne
		@JoinColumn(name="reporter_fk")
		@Fetch( FetchMode.JOIN )
		private User reporter;
		@ManyToOne
		@JoinColumn(name="assignee_fk")
		@Fetch( FetchMode.SELECT )
		private User assignee;

		public Issue() {
		}

		public Issue(Integer id, String description, User reporter) {
			this.id = id;
			this.description = description;
			this.reporter = reporter;
		}

		public Issue(Integer id, String description, User reporter, User assignee) {
			this.id = id;
			this.description = description;
			this.reporter = reporter;
			this.assignee = assignee;
		}
	}

	@Entity(name="User")
	@Table(name="users")
	@SoftDelete(columnName = "active", converter = ReverseYesNoConverter.class)
	public static class User {
		@Id
		private Integer id;
		private String name;

		public User() {
		}

		public User(Integer id, String name) {
			this.id = id;
			this.name = name;
		}
	}
}
