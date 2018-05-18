/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression;

import org.hibernate.QueryException;
import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.exec.spi.ParameterBindingContext;
import org.jboss.logging.Logger;

/**
 * Represents a named parameter coming from the query.
 *
 * @author Christian Beikov
 */
public class AnonymousParameter extends AbstractParameter {
	private static final Logger log = Logger.getLogger( AnonymousParameter.class );

	private final QueryParameterImplementor<?> queryParameter;

	public AnonymousParameter(QueryParameterImplementor<?> queryParameter, AllowableParameterType inferredType) {
		super( inferredType );
		this.queryParameter = queryParameter;
	}

	public QueryParameter<?> getQueryParameter() {
		return queryParameter;
	}

	@Override
	public QueryParameterBinding resolveBinding(ParameterBindingContext context) {
		return context.getQueryParameterBindings().getBinding( queryParameter );
	}

	@Override
	protected void warnNoBinding() {
		log.debugf( "Query defined anonymous parameter [%s], but no binding was found (setParameter not called)", getQueryParameter() );
	}

	@Override
	protected void unresolvedType() {
		throw new QueryException( "Unable to determine Type for anonymous parameter [" + getQueryParameter() + "]" );
	}

	@Override
	protected void warnNullBindValue() {
		log.debugf( "Binding value for anonymous parameter [%s] was null", getQueryParameter() );
	}

	@Override
	public void accept(SqlAstWalker  walker) {
		walker.visitAnonymousParameter( this );
	}
}
