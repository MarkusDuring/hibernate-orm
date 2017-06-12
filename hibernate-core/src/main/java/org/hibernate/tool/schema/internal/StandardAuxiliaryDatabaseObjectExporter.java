/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.tool.schema.internal;

import org.hibernate.metamodel.model.relational.spi.AuxiliaryDatabaseObject;
import org.hibernate.metamodel.model.relational.spi.DatabaseModel;
import org.hibernate.tool.schema.spi.Exporter;

/**
 * @author Steve Ebersole
 */
public class StandardAuxiliaryDatabaseObjectExporter implements Exporter<AuxiliaryDatabaseObject> {

	@Override
	public String[] getSqlCreateStrings(AuxiliaryDatabaseObject object, DatabaseModel databaseMode) {
		return object.getSqlCreateStrings();
	}

	@Override
	public String[] getSqlDropStrings(AuxiliaryDatabaseObject object, DatabaseModel databaseMode) {
		return object.getSqlDropStrings();
	}
}
