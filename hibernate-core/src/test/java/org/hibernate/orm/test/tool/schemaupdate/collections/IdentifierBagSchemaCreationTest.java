/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.tool.schemaupdate.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.orm.test.tool.BaseSchemaUnitTestCase;
import org.hibernate.orm.test.tool.util.RecordingTarget;
import org.hibernate.tool.schema.internal.exec.GenerationTargetToStdout;

import org.hibernate.testing.junit5.RequiresDialect;
import org.hibernate.testing.junit5.schema.SchemaScope;
import org.hibernate.testing.junit5.schema.SchemaTest;
import org.junit.jupiter.api.Disabled;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Andrea Boriero
 */
@RequiresDialect(dialectClass = H2Dialect.class, matchSubTypes = true)
public class IdentifierBagSchemaCreationTest extends BaseSchemaUnitTestCase {
	@Override
	protected void applySettings(StandardServiceRegistryBuilder serviceRegistryBuilder) {
		serviceRegistryBuilder.applySetting( AvailableSettings.FORMAT_SQL, false );
	}

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] { Item.class };
	}

	@Override
	protected boolean createSqlScriptTempOutputFile() {
		return true;
	}

	@Override
	protected boolean dropSchemaAfterTest() {
		return false;
	}

	@SchemaTest
	public void testElementAndCollectionTableAreCreated(SchemaScope scope) {
		final RecordingTarget target = new RecordingTarget( getDialect() );
		scope.withSchemaCreator(
				null,
				schemaCreator -> schemaCreator.doCreation(
						true,
						target,
						new GenerationTargetToStdout()
				)
		);

		assertThat(
				target.getActions( target.tableCreateActions() ),
				target.containsExactly(
						"item (id bigint not null, primary key (id))",
						"image (image_id bigint not null, image_name varchar(255), item_id bigint not null, primary key (image_id))"
				)
		);

		assertTrue(
				target.containsAction(
						Pattern.compile(
								"alter table image add constraint (.*) foreign key \\(item_id\\) references item \\(id\\)" )
				),
				"The expected foreign key has not been generated"
		);
	}


	@Entity(name = "Item")
	@Table(name = "ITEM")
	@GenericGenerator(name = "increment", strategy = "increment")
	public static class Item {
		@Id
		private Long id;

		@ElementCollection
		@CollectionTable(name = "IMAGE", joinColumns = @JoinColumn(name = "ITEM_ID"))
		@Column(name = "IMAGE_NAME")
		@CollectionId(columns = @Column(name = "IMAGE_ID"),
				type = @Type(type = "long"), generator = "increment")
		private Collection<String> images = new ArrayList<>();
	}
}
