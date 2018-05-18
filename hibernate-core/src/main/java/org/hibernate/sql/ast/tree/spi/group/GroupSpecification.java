/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.group;

import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.tree.spi.SqlAstNode;
import org.hibernate.sql.ast.tree.spi.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class GroupSpecification implements SqlAstNode {
	private final Expression groupExpression;
	private final String collation;

	public GroupSpecification(Expression groupExpression, String collation) {
		this.groupExpression = groupExpression;
		this.collation = collation;
	}

	public Expression getGroupExpression() {
		return groupExpression;
	}

	public String getCollation() {
		return collation;
	}

	@Override
	public void accept(SqlAstWalker sqlTreeWalker) {
		sqlTreeWalker.visitGroupSpecification( this );
	}
}
