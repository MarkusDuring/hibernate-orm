/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.produce.internal;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.consume.spi.BaseSemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.expression.SqmAnonymousParameter;
import org.hibernate.query.sqm.tree.expression.SqmNamedParameter;
import org.hibernate.query.sqm.tree.expression.SqmPositionalParameter;
import org.hibernate.query.sqm.tree.select.SqmDynamicInstantiation;
import org.hibernate.query.sqm.tree.select.SqmSelectionBase;

import javax.persistence.criteria.ParameterExpression;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 */
public class SqmParameterCollector extends BaseSemanticQueryWalker {

	private final Set<ParameterExpression<?>> parameters = new HashSet<>();

	private SqmParameterCollector(SessionFactoryImplementor sessionFactory) {
		super( sessionFactory );
	}

	public static Set<ParameterExpression<?>> getParameters(SqmSelectStatement statement, SessionFactoryImplementor sessionFactory) {
		SqmParameterCollector collector = new SqmParameterCollector( sessionFactory );
		collector.visitSelectStatement( statement );
		return collector.parameters;
	}

	@Override
	public Object visitSelection(SqmSelectionBase selection) {
		// todo : this is only necessary because SqmDynamicInstantiation currently seems to be buggy. we can remove this method after fixing the problems there
		if ( selection.getSelectableNode() instanceof SqmDynamicInstantiation ) {
			SqmDynamicInstantiation dynamicInstantiation = (SqmDynamicInstantiation) selection.getSelectableNode();
			for ( SqmSelectionBase argument : dynamicInstantiation.getArguments() ) {
				visitSelection( argument );
			}
		} else {
			selection.getSelectableNode().accept( this );
		}
		return null;
	}

	@Override
	public Object visitPositionalParameterExpression(SqmPositionalParameter expression) {
		parameters.add( expression );
		return null;
	}

	@Override
	public Object visitNamedParameterExpression(SqmNamedParameter expression) {
		parameters.add( expression );
		return null;
	}

	@Override
	public Object visitAnonymousParameterExpression(SqmAnonymousParameter expression) {
		parameters.add( expression );
		return null;
	}
}
