/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.community.dialect.identity;

/**
 * @author Andrea Boriero
 */
public class Ingres10IdentityColumnSupport extends Ingres9IdentityColumnSupport {

	public static final Ingres10IdentityColumnSupport INSTANCE = new Ingres10IdentityColumnSupport();

	@Override
	public boolean supportsIdentityColumns() {
		return true;
	}

	@Override
	public boolean hasDataTypeInIdentityColumn() {
		return true;
	}

	@Override
	public String getIdentityColumnString(int type) {
		return "not null generated by default as identity";
	}

	@Override
	public String getIdentityInsertString() {
		return "default";
	}
}
