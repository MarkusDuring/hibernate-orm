/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.QueryResultProducer;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * Represents the {@code SIZE()} function.
 *
 * @author Steve Ebersole
 * @author Gunnar Morling
 */
public class SqmCollectionSize extends AbstractSqmExpression implements SqmExpression, QueryResultProducer {
	private final SqmPluralAttributeReference pluralAttributeBinding;
	private final BasicValuedExpressableType sizeType;

	public SqmCollectionSize(
			SessionFactoryImplementor sessionFactory,
			SqmPluralAttributeReference pluralAttributeBinding,
			BasicValuedExpressableType sizeType) {
		super( sessionFactory );
		this.pluralAttributeBinding = pluralAttributeBinding;
		this.sizeType = sizeType;
	}

	public SqmPluralAttributeReference getPluralAttributeBinding() {
		return pluralAttributeBinding;
	}

	@Override
	public BasicValuedExpressableType getExpressableType() {
		return sizeType;
	}

	@Override
	public BasicValuedExpressableType getInferableType() {
		return getExpressableType();
	}

	@Override
	public SqmCollectionSize copy(SqmCopyContext context) {
		return new SqmCollectionSize(
                getSessionFactory(),
				pluralAttributeBinding.copy( context ),
				sizeType
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitPluralAttributeSizeFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "SIZE(" + pluralAttributeBinding.asLoggableText() + ")";
	}

	@Override
	public QueryResult createQueryResult(
			String resultVariable,
			QueryResultCreationContext creationContext) {
		throw new NotYetImplementedFor6Exception(  );
//		return new ScalarQueryResultImpl(
//				resultVariable,
//				creationContext.getSqlSelectionResolver().resolveSqlSelection( expression ),
//				getExpressableType()
//		);
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getExpressableType().getJavaTypeDescriptor();
	}
}
