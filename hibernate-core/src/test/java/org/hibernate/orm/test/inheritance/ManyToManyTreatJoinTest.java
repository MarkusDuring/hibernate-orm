/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.inheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.JiraKey;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@JiraKey("HHH-")
@DomainModel(
		annotatedClasses = {
				ManyToManyTreatJoinTest.JoinedBase.class,
				ManyToManyTreatJoinTest.JoinedSub1.class,
				ManyToManyTreatJoinTest.JoinedSub2.class
		}
)
@SessionFactory
public class ManyToManyTreatJoinTest {

	@Test
	public void testQueryTreatJoinManyToMany(SessionFactoryScope scope) {
		scope.inTransaction( session -> {
			session.createSelectionQuery("select c.name2 from JoinedBase t join treat(t.subMap as JoinedSub2) c", String.class)
				.getResultList();
		} );
	}

	@BeforeEach
	public void setupData(SessionFactoryScope scope) {
		scope.inTransaction( session -> {
			JoinedSub1 o1 = new JoinedSub1(1 );
			JoinedSub2 o2 = new JoinedSub2(2 );

			session.persist( o2 );
			session.persist( o1 );

			o1.subMap.put(2, o2 );
		} );
	}

	@AfterEach
	public void cleanupData(SessionFactoryScope scope) {
		scope.inTransaction( session -> {
			session.createQuery( "from JoinedBase", JoinedBase.class )
					.getResultList()
					.forEach( session::remove );
		} );
	}

	@Entity(name = "JoinedBase")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static abstract class JoinedBase {
		@Id
		Integer id;
		String name;
		@ManyToMany
		@JoinTable(name = "sub_map")
		Map<Integer, JoinedBase> subMap = new HashMap<>();

		public JoinedBase() {
		}

		public JoinedBase(Integer id) {
			this.id = id;
		}
	}

	@Entity(name = "JoinedSub1")
	@Table(name = "joined_sub_1")
	public static class JoinedSub1 extends JoinedBase {

		String name1;

		public JoinedSub1() {
		}

		public JoinedSub1(Integer id) {
			super( id );
		}
	}

	@Entity(name = "JoinedSub2")
	@Table(name = "joined_sub_2")
	public static class JoinedSub2 extends JoinedBase {

		String name2;

		public JoinedSub2() {
		}

		public JoinedSub2(Integer id) {
			super( id );
		}
	}
}
