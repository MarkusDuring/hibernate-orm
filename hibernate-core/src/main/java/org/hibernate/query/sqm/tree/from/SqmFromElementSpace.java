/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.query.sqm.produce.internal.SqmFromBuilderFromClauseQualifiedJoin;
import org.hibernate.query.sqm.produce.spi.SqmFromBuilder;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.jboss.logging.Logger;

/**
 * Contract representing a "from element space", which is a particular root FromElement along with a list of
 * its related joins.  A list is used specifically because the order is important (!) in terms of left/right.
 *
 * SQL calls this a "table reference".  It views all the tables joined together
 * as a single unit separated by the columns.
 *
 * @author Steve Ebersole
 */
public class SqmFromElementSpace {
	private static final Logger log = Logger.getLogger( SqmFromElementSpace.class );

	private final SqmFromClause fromClause;

	private SqmRoot root;
	private List<SqmJoin> joins;

	public SqmFromElementSpace(SqmFromClause fromClause) {
		this.fromClause = fromClause;
	}

	public SqmFromElementSpace copy(SqmFromClause fromClause, SqmCopyContext context) {
		return context.copy( this, () -> {
			final SqmFromBuilder oldFromBuilder = context.getCreationContext().getCurrentFromElementBuilder();
			final SqmFromElementSpace oldElementSpace = context.getCreationContext().getCurrentFromElementSpace();
			try {
				final SqmFromElementSpace fromElementSpace = new SqmFromElementSpace( fromClause );
				context.getCreationContext().getCurrentSqmFromElementSpaceCoordAccess()
						.setCurrentSqmFromElementSpace( fromElementSpace );

				fromElementSpace.setRoot( root == null ? null : root.copy( context ) );

				if ( joins != null ) {
					List<SqmJoin> newJoins = new ArrayList<>( joins.size() );
					for ( SqmJoin join : joins ) {
						final SqmFromBuilder fromBuilder = new SqmFromBuilderFromClauseQualifiedJoin(
								join.getSqmJoinType(),
								join instanceof SqmNavigableJoin && ((SqmNavigableJoin) join).isFetched(),
								join.getIdentificationVariable(),
								context.getCreationContext()
						);
						context.getCreationContext().setCurrentFromElementBuilder( fromBuilder );
						newJoins.add( context.copy(
								join,
								() -> join.copy( context )
						));
					}

					fromElementSpace.joins = newJoins;
				}
				return fromElementSpace;
			} finally {
				context.getCreationContext().getCurrentSqmFromElementSpaceCoordAccess()
						.setCurrentSqmFromElementSpace( oldElementSpace );
				context.getCreationContext().setCurrentFromElementBuilder( oldFromBuilder );
			}
		});
	}

	public SqmFromClause getFromClause() {
		return fromClause;
	}

	public SqmRoot getRoot() {
		return root;
	}

	public void setRoot(SqmRoot root) {
		if ( this.root != null ) {
			// we already had a root defined...
			if ( this.root == root ) {
				// its the same object reference, so no worries
				return;
			}
			else {
				// todo : error or warning?
				log.warn( "FromElementSpace#setRoot called when a root was already defined" );
			}
		}
		this.root = root;
	}

	public List<SqmJoin> getJoins() {
		return joins == null ? Collections.emptyList() : joins;
	}

	public void addJoin(SqmJoin join) {
		if ( joins == null ) {
			joins = new ArrayList<>();
		}
		joins.add( join );
	}
}
