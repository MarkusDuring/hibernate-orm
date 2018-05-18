/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.type.descriptor.java.internal.JdbcTimestampJavaDescriptor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Steve Ebersole
 * @author Christian Beikov
 */
public class SqmLiteralTimestamp extends AbstractSqmLiteral<Date> {
	public static SqmLiteralTimestamp from(String literalText, SqmCreationContext creationContext) {
		final Timestamp literal = Timestamp.valueOf(
				LocalDateTime.from( JdbcTimestampJavaDescriptor.FORMATTER.parse( literalText ) )
		);

		return new SqmLiteralTimestamp(
				creationContext.getSessionFactory(),
				literal,
				creationContext.getSessionFactory().getTypeConfiguration().getBasicTypeRegistry().getBasicType( Timestamp.class )
		);
	}

	public SqmLiteralTimestamp(SessionFactoryImplementor sessionFactory, Date timestamp, BasicValuedExpressableType sqmExpressableTypeBasic) {
		super( sessionFactory, timestamp, sqmExpressableTypeBasic );
	}

	@Override
	public SqmLiteralTimestamp copy(SqmCopyContext context) {
		return new SqmLiteralTimestamp(
                getSessionFactory(),
				(Date) getLiteralValue().clone(),
				getExpressableType()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralTimestampExpression( this );
	}

}
