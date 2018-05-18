/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.query.criteria;

import org.hibernate.Session;
import org.hibernate.boot.MetadataSources;
import org.hibernate.orm.test.SessionFactoryBasedFunctionalTest;
import org.hibernate.query.spi.QueryImplementor;
import org.hibernate.query.sqm.tree.expression.*;
import org.junit.jupiter.api.Test;

import javax.persistence.*;
import javax.persistence.criteria.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This can be removed once we re-enable the original tests.
 *
 * @author Christian Beikov
 */
public class FirstCriteriaTest extends SessionFactoryBasedFunctionalTest {

	@Entity( name = "Person" )
	public static class Person {
		@Id
		public Integer id;
		public String name;
		public Kind kind;
		@ManyToOne(fetch = FetchType.LAZY)
		public Person parent;
		@OneToMany
		@OrderColumn
		public List<Person> people;

		public Person() {
		}

		public Person(Integer id, String name, Kind kind) {
			this.id = id;
			this.name = name;
			this.kind = kind;
		}
	}

	public static enum Kind {
		A,
		B;
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );
		metadataSources.addAnnotatedClass( Person.class );
	}

	@Override
	protected boolean exportSchema() {
		return true;
	}

	@Test
	public void testBla() {
		sessionFactoryScope().inTransaction(
				session -> {
					session.doWork(
							connection -> {
								final Statement statement = connection.createStatement();
								try {
									statement.execute(
											"insert into Person( id, name, kind ) values ( 1, 'A', 0 )" );
									statement.execute(
											"insert into Person( id, name, kind ) values ( 2, 'B', 1 )" );
									statement.execute(
											"insert into Person( id, name, kind ) values ( 3, 'C', NULL )" );
								}
								finally {
									try {
										statement.close();
									}
									catch (SQLException ignore) {
									}
								}
							}
					);
//					session.persist( new Person( 1, "A", Kind.A ) );
//					session.persist( new Person( 2, "B", Kind.B ) );
//					session.persist( new Person( 3, "C", null ) );

					CriteriaBuilder cb = session.getCriteriaBuilder();
					CriteriaQuery<Object> cq = cb.createQuery();
					Root<Person> p = cq.from( Person.class );
					cq.select( p.get( "name" ) );
//					p.fetch( "parent" );

					Subquery<Long> subquery = cq.subquery( Long.class );
					Root<Person> subqueryP = subquery.from( Person.class ); //subquery.correlate( p );
					subquery.select( cb.count( subqueryP ) );
					subquery.where( subqueryP.get( "parent" ).isNull() );

//					cq.where(
//							cb.and(
//								cb.gt( subquery, 1 ),
//								p.get( "kind" ).isNotNull()
//							)
//					);
					cq.where( p.get( "kind" ).isNotNull() );
					cq.orderBy( cb.asc( p.get( "id" ) ) );

					QueryImplementor<Object> query = session.createQuery( cq );
					List<Object> resultList = query.getResultList();

					assertThat( resultList.size(), is( 2 ) );
					assertThat( resultList.get( 0 ), is( "A" ) );
					assertThat( resultList.get( 1 ), is( "B" ) );
				}
		);
	}

	@Test
	public void testNullLiteralValues() {
		sessionFactoryScope().inTransaction(
				session -> {
					assertNullLiteral( session, boolean.class );
					assertNullLiteral( session, Boolean.class );
					assertNullLiteral( session, byte.class );
					assertNullLiteral( session, Byte.class );
					assertNullLiteral( session, short.class );
					assertNullLiteral( session, Short.class );
					assertNullLiteral( session, int.class );
					assertNullLiteral( session, Integer.class );
					assertNullLiteral( session, float.class );
					assertNullLiteral( session, Float.class );
					assertNullLiteral( session, double.class );
					assertNullLiteral( session, Double.class );
					assertNullLiteral( session, BigInteger.class );
					assertNullLiteral( session, BigDecimal.class );
					assertNullLiteral( session, char.class );
					assertNullLiteral( session, Character.class );
					assertNullLiteral( session, String.class );
					assertNullLiteral( session, java.util.Date.class );
					assertNullLiteral( session, java.util.Calendar.class );
					assertNullLiteral( session, java.util.GregorianCalendar.class );
					assertNullLiteral( session, java.sql.Date.class );
					assertNullLiteral( session, java.sql.Time.class );
					assertNullLiteral( session, java.sql.Timestamp.class );
					assertNullLiteral( session, java.time.LocalDate.class );
					assertNullLiteral( session, java.time.LocalDateTime.class );
					assertNullLiteral( session, java.time.LocalTime.class );
					assertNullLiteral( session, java.time.OffsetDateTime.class );
					assertNullLiteral( session, java.time.OffsetTime.class );

					assertNullLiteral( session, Person.class );
					assertNullLiteral( session, Kind.class );
				}
		);
	}

	private void assertNullLiteral(Session session, Class<?> type) {
		Expression<?> expression = session.getCriteriaBuilder().nullLiteral(type);
		assertThat( expression, is( instanceOf( SqmLiteralNull.class ) ) );
		assertThat( expression.getJavaType(), is( equalTo( type ) ) );
	}

	@Test
	public void testLiteralValues() {
		sessionFactoryScope().inTransaction(
			session -> {
				assertThat(
						session.getCriteriaBuilder().literal( true ),
						is( instanceOf( SqmLiteralTrue.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( false ),
						is( instanceOf( SqmLiteralFalse.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( (byte) 1 ),
						is( instanceOf( SqmLiteralGeneric.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( (short) 1 ),
						is( instanceOf( SqmLiteralGeneric.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( 1 ),
						is( instanceOf( SqmLiteralInteger.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( 1L ),
						is( instanceOf( SqmLiteralLong.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( 1F ),
						is( instanceOf( SqmLiteralFloat.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( 1D ),
						is( instanceOf( SqmLiteralDouble.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( BigInteger.ZERO ),
						is( instanceOf( SqmLiteralBigInteger.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( BigDecimal.ZERO ),
						is( instanceOf( SqmLiteralBigDecimal.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( ' ' ),
						is( instanceOf( SqmLiteralCharacter.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( "" ),
						is( instanceOf( SqmLiteralString.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( new java.util.Date() ),
						is( instanceOf( SqmLiteralTimestamp.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( Calendar.getInstance() ),
						is( instanceOf( SqmLiteralGeneric.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( new GregorianCalendar() ),
						is( instanceOf( SqmLiteralGeneric.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( new java.sql.Date( Clock.systemDefaultZone().millis() ) ),
						is( instanceOf( SqmLiteralDate.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( new java.sql.Time( Clock.systemDefaultZone().millis() ) ),
						is( instanceOf( SqmLiteralTime.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( new java.sql.Timestamp( Clock.systemDefaultZone().millis() ) ),
						is( instanceOf( SqmLiteralTimestamp.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( java.time.LocalDate.now() ),
						is( instanceOf( SqmLiteralGeneric.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( java.time.LocalDateTime.now() ),
						is( instanceOf( SqmLiteralGeneric.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( java.time.LocalTime.now() ),
						is( instanceOf( SqmLiteralGeneric.class ) )
				);
				// todo : do we want to support this? there is no JDBC syntax for timestamp with timezone..
//				assertThat(
//						session.getCriteriaBuilder().literal( java.time.OffsetDateTime.now() ),
//						is( instanceOf( SqmLiteralGeneric.class ) )
//				);
//				assertThat(
//						session.getCriteriaBuilder().literal( java.time.OffsetTime.now() ),
//						is( instanceOf( SqmLiteralGeneric.class ) )
//				);

				assertThat(
						session.getCriteriaBuilder().literal( Person.class ),
						is( instanceOf( SqmLiteralEntityType.class ) )
				);
				assertThat(
						session.getCriteriaBuilder().literal( Kind.A ),
						is( instanceOf( SqmConstantEnum.class ) )
				);
			}
		);
	}
}
