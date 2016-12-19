/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.bytecode.enhance.spi.LazyPropertyInitializer;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.entity.spi.EntityPersister;
import org.hibernate.property.access.internal.PropertyAccessStrategyBackRefImpl;
import org.hibernate.type.spi.ColumnMapping;
import org.hibernate.type.spi.Type;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * A many-to-one association to an entity.
 *
 * @author Gavin King
 */
public class ManyToOneType extends AbstractEntityType {
	private final boolean ignoreNotFound;
	private boolean isLogicalOneToOne;

	/**
	 * Creates a many-to-one association type with the given referenced entity.
	 *
	 * @param typeConfiguration The TypeConfiguration for this instance.
	 * @param referencedEntityName The name iof the referenced entity
	 */
	public ManyToOneType(TypeConfiguration typeConfiguration, String referencedEntityName) {
		this( typeConfiguration, referencedEntityName, false );
	}

	/**
	 * Creates a many-to-one association type with the given referenced entity and the
	 * given laziness characteristic
	 *
	 * @param typeConfiguration The TypeConfiguration for this instance.
	 * @param referencedEntityName The name iof the referenced entity
	 * @param lazy Should the association be handled lazily
	 */
	public ManyToOneType(TypeConfiguration typeConfiguration, String referencedEntityName, boolean lazy) {
		this( typeConfiguration, referencedEntityName, true, null, lazy, true, false, false );
	}


	public ManyToOneType(
			TypeConfiguration typeConfiguration,
			String referencedEntityName,
			boolean referenceToPrimaryKey,
			String uniqueKeyPropertyName,
			boolean lazy,
			boolean unwrapProxy,
			boolean ignoreNotFound,
			boolean isLogicalOneToOne) {
		super( typeConfiguration, referencedEntityName, referenceToPrimaryKey, uniqueKeyPropertyName, !lazy, unwrapProxy );
		this.ignoreNotFound = ignoreNotFound;
		this.isLogicalOneToOne = isLogicalOneToOne;
	}

	@Override
	protected boolean isNullable() {
		return ignoreNotFound;
	}

	@Override
	public boolean isAlwaysDirtyChecked() {
		// always need to dirty-check, even when non-updateable;
		// this ensures that when the association is updated,
		// the entity containing this association will be updated
		// in the cache
		return true;
	}

	@Override
	public boolean isOneToOne() {
		return false;
	}

	@Override
	public boolean isLogicalOneToOne() {
		return isLogicalOneToOne;
	}

	@Override
	public int getColumnSpan() throws MappingException {
		return requireIdentifierOrUniqueKeyType().getColumnSpan();
	}

	private Type requireIdentifierOrUniqueKeyType() {
		final Type fkTargetType = getIdentifierOrUniqueKeyType();
		if ( fkTargetType == null ) {
			throw new MappingException(
					"Unable to determine FK target Type for many-to-one mapping: " +
							"referenced-entity-name=[" + getAssociatedEntityName() +
							"], referenced-entity-attribute-name=[" + getLHSPropertyName() + "]"
			);
		}
		return fkTargetType;
	}

	@Override
	public int[] sqlTypes() throws MappingException {
		return requireIdentifierOrUniqueKeyType().sqlTypes();
	}

	@Override
	public Classification getClassification() {
		return Classification.ENTITY;
	}

	@Override
	public ColumnMapping[] getColumnMappings() {
		return requireIdentifierOrUniqueKeyType( ).getColumnMappings();
	}

	@Override
	public void nullSafeSet(
			PreparedStatement st,
			Object value,
			int index,
			boolean[] settable,
			SharedSessionContractImplementor session) throws HibernateException, SQLException {
		requireIdentifierOrUniqueKeyType()
				.nullSafeSet( st, getIdentifier( value, session ), index, settable, session );
	}

	@Override
	public void nullSafeSet(
			PreparedStatement st,
			Object value,
			int index,
			SharedSessionContractImplementor session) throws HibernateException, SQLException {
		requireIdentifierOrUniqueKeyType()
				.nullSafeSet( st, getIdentifier( value, session ), index, session );
	}

	@Override
	public ForeignKeyDirection getForeignKeyDirection() {
		return ForeignKeyDirection.FROM_PARENT;
	}

	@Override
	public Object hydrate(
			ResultSet rs,
			String[] names,
			SharedSessionContractImplementor session,
			Object owner) throws HibernateException, SQLException {
		// return the (fully resolved) identifier value, but do not resolve
		// to the actual referenced entity instance
		// NOTE: the owner of the association is not really the owner of the id!
		final Serializable id = (Serializable) getIdentifierOrUniqueKeyType()
				.nullSafeGet( rs, names, session, null );
		scheduleBatchLoadIfNeeded( id, session );
		return id;
	}

	/**
	 * Register the entity as batch loadable, if enabled
	 */
	@SuppressWarnings({ "JavaDoc" })
	private void scheduleBatchLoadIfNeeded(Serializable id, SharedSessionContractImplementor session) throws MappingException {
		//cannot batch fetch by unique key (property-ref associations)
		if ( uniqueKeyPropertyName == null && id != null ) {
			final EntityPersister persister = getAssociatedEntityPersister( session.getFactory() );
			if ( persister.isBatchLoadable() ) {
				final EntityKey entityKey = session.generateEntityKey( id, persister );
				if ( !session.getPersistenceContext().containsEntity( entityKey ) ) {
					session.getPersistenceContext().getBatchFetchQueue().addBatchLoadableEntityKey( entityKey );
				}
			}
		}
	}

	@Override
	public boolean useLHSPrimaryKey() {
		return false;
	}

	@Override
	public boolean isModified(
			Object old,
			Object current,
			boolean[] checkable,
			SharedSessionContractImplementor session) throws HibernateException {
		if ( current == null ) {
			return old!=null;
		}
		if ( old == null ) {
			// we already know current is not null...
			return true;
		}
		// the ids are fully resolved, so compare them with isDirty(), not isModified()
		return getIdentifierOrUniqueKeyType()
				.isDirty( old, getIdentifier( current, session ), session );
	}

	@Override
	public Serializable disassemble(
			Object value,
			SharedSessionContractImplementor session,
			Object owner) throws HibernateException {

		if ( value == null ) {
			return null;
		}
		else {
			// cache the actual id of the object, not the value of the
			// property-ref, which might not be initialized
			Object id = ForeignKeys.getEntityIdentifierIfNotUnsaved(
					getAssociatedEntityName(),
					value,
					session
			);
			if ( id == null ) {
				throw new AssertionFailure(
						"cannot cache a reference to an object with a null id: " + 
						getAssociatedEntityName()
				);
			}
			return getIdentifierType( session ).disassemble( id, session, owner );
		}
	}

	@Override
	public Object assemble(
			Serializable oid,
			SharedSessionContractImplementor session,
			Object owner) throws HibernateException {
		if ( oid != LazyPropertyInitializer.UNFETCHED_PROPERTY
				&& oid != PropertyAccessStrategyBackRefImpl.UNKNOWN ) {
			scheduleBatchLoadIfNeeded( assembleId( oid, session ), session );
		}

		//TODO: currently broken for unique-key references (does not detect
		//      change to unique key property of the associated object)
		
		Serializable id = assembleId( oid, session );

		if ( id == null ) {
			return null;
		}
		else {
			return resolveIdentifier( id, session );
		}
	}

	private Serializable assembleId(Serializable oid, SharedSessionContractImplementor session) {
		//the owner of the association is not the owner of the id
		return ( Serializable ) getIdentifierType( session ).assemble( oid, session, null );
	}

	@Override
	public boolean[] toColumnNullness(Object value) {
		boolean[] result = new boolean[getColumnSpan()];
		if ( value != null ) {
			Arrays.fill( result, true );
		}
		return result;
	}

	@Override
	public boolean isDirty(
			Object old,
			Object current,
			SharedSessionContractImplementor session) throws HibernateException {
		if ( isSame( old, current ) ) {
			return false;
		}
		Object oldid = getIdentifier( old, session );
		Object newid = getIdentifier( current, session );
		return getIdentifierType( session ).isDirty( oldid, newid, session );
	}

	@Override
	public boolean isDirty(
			Object old,
			Object current,
			boolean[] checkable,
			SharedSessionContractImplementor session) throws HibernateException {
		if ( isAlwaysDirtyChecked() ) {
			return isDirty( old, current, session );
		}
		else {
			if ( isSame( old, current ) ) {
				return false;
			}
			Object oldid = getIdentifier( old, session );
			Object newid = getIdentifier( current, session );
			return getIdentifierType( session ).isDirty( oldid, newid, checkable, session );
		}
		
	}

}
