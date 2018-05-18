/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import java.sql.Date;
import java.time.LocalDate;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.type.descriptor.java.internal.JdbcDateJavaDescriptor;

/**
 * @author Steve Ebersole
 * @author Christian Beikov
 */
public class SqmLiteralDate extends AbstractSqmLiteral<java.util.Date> {
	public static SqmLiteralDate from(String literalText, SqmCreationContext creationContext) {
		final LocalDate localDate = LocalDate.from( JdbcDateJavaDescriptor.FORMATTER.parse( literalText ) );
		final Date literal = new Date( localDate.toEpochDay() );

		return new SqmLiteralDate(
				creationContext.getSessionFactory(),
				literal,
				creationContext.getSessionFactory().getTypeConfiguration().getBasicTypeRegistry().getBasicType( Date.class )
		);
	}

    public SqmLiteralDate(SessionFactoryImplementor sessionFactory, java.util.Date date, BasicValuedExpressableType sqmExpressableTypeBasic) {
        super( sessionFactory, date, sqmExpressableTypeBasic );
    }

	@Override
	public SqmLiteralDate copy(SqmCopyContext context) {
		return new SqmLiteralDate(
				getSessionFactory(),
				(Date) getLiteralValue().clone(),
				getExpressableType()
		);
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralDateExpression( this );
	}

}
