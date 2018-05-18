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
import org.hibernate.type.descriptor.java.internal.JdbcTimeJavaDescriptor;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Date;

/**
 * @author Steve Ebersole
 * @author Christian Beikov
 */
public class SqmLiteralTime extends AbstractSqmLiteral<Date> {
	public static SqmLiteralTime from(String literalText, SqmCreationContext creationContext) {
		final LocalTime localTime = LocalTime.from( JdbcTimeJavaDescriptor.FORMATTER.parse( literalText ) );
		final Time literal = Time.valueOf( localTime );

		return new SqmLiteralTime(
				creationContext.getSessionFactory(),
				literal,
				creationContext.getSessionFactory().getTypeConfiguration().getBasicTypeRegistry().getBasicType( Time.class )
		);
	}

	public SqmLiteralTime(SessionFactoryImplementor sessionFactory, Date time, BasicValuedExpressableType sqmExpressableTypeBasic) {
		super( sessionFactory, time, sqmExpressableTypeBasic );
	}

	@Override
	public SqmLiteralTime copy(SqmCopyContext context) {
		return new SqmLiteralTime(
                getSessionFactory(),
				(Date) getLiteralValue().clone(),
				getExpressableType()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralTimeExpression( this );
	}

}
