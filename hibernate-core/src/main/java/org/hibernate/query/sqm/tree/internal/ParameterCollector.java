/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.internal;

import org.hibernate.query.sqm.tree.expression.SqmAnonymousParameter;
import org.hibernate.query.sqm.tree.expression.SqmNamedParameter;
import org.hibernate.query.sqm.tree.expression.SqmPositionalParameter;

/**
 * @author Steve Ebersole
 */
public interface ParameterCollector {
	void addParameter(SqmNamedParameter parameter);
	void addParameter(SqmPositionalParameter parameter);
	void addParameter(SqmAnonymousParameter parameter);
}
