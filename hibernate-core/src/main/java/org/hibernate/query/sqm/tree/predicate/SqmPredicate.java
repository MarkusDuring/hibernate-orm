/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.predicate;

import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmNode;
import org.hibernate.query.sqm.tree.SqmTypedNode;
import org.hibernate.query.sqm.tree.SqmVisitableNode;
import org.hibernate.type.descriptor.java.internal.BooleanJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.Collection;

/**
 * @author Steve Ebersole
 */
public interface SqmPredicate extends SqmVisitableNode, SqmTypedNode, Predicate {

	@Override
	SqmPredicate copy(SqmCopyContext context);

	@Override
	SqmPredicate not();

	@Override
	SqmPredicate isNull();

	@Override
	SqmPredicate isNotNull();

	@Override
	SqmPredicate in(Object... values);

	@Override
	SqmPredicate in(Expression<?>... values);

	@Override
	SqmPredicate in(Collection<?> values);

	@Override
	SqmPredicate in(Expression<Collection<?>> values);

	@Override
	default JavaTypeDescriptor<Boolean> getJavaTypeDescriptor(){
		return BooleanJavaDescriptor.INSTANCE;
	}
}
