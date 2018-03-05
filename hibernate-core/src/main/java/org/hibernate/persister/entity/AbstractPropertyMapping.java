/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.persister.entity;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.mapping.*;
import org.hibernate.sql.Template;
import org.hibernate.type.AnyType;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.OneToOneType;
import org.hibernate.type.SpecialOneToOneType;
import org.hibernate.type.Type;

/**
 * Basic implementation of the {@link PropertyMapping} contract.
 *
 * @author Gavin King
 */
public abstract class AbstractPropertyMapping implements PropertyMapping {
	private static final CoreMessageLogger LOG = CoreLogging.messageLogger( AbstractPropertyMapping.class );

	private final Map<String, Type> typesByPropertyPath = new HashMap<String, Type>();
	private final Map<String, String[]> columnsByPropertyPath = new HashMap<String, String[]>();
	private final Map<String, String[]> columnReadersByPropertyPath = new HashMap<String, String[]>();
	private final Map<String, String[]> columnReaderTemplatesByPropertyPath = new HashMap<String, String[]>();
	private final Map<String, String[]> formulaTemplatesByPropertyPath = new HashMap<String, String[]>();

	public String[] getIdentifierColumnNames() {
		throw new UnsupportedOperationException( "one-to-one is not supported here" );
	}

	public String[] getIdentifierColumnReaderTemplates() {
		throw new UnsupportedOperationException( "one-to-one is not supported here" );
	}

	public String[] getIdentifierColumnReaders() {
		throw new UnsupportedOperationException( "one-to-one is not supported here" );
	}

	protected abstract String getEntityName();

	public Type toType(String propertyName) throws QueryException {
		Type type = typesByPropertyPath.get( propertyName );
		if ( type == null ) {
			throw propertyException( propertyName );
		}
		return type;
	}

	protected final QueryException propertyException(String propertyName) throws QueryException {
		return new QueryException( "could not resolve property: " + propertyName + " of: " + getEntityName() );
	}

	public String[] getColumnNames(String propertyName) {
		String[] cols = columnsByPropertyPath.get( propertyName );
		if ( cols == null ) {
			throw new MappingException( "unknown property: " + propertyName );
		}
		return cols;
	}

	public String[] toColumns(String alias, String propertyName) throws QueryException {
		//TODO: *two* hashmap lookups here is one too many...
		String[] columns = columnsByPropertyPath.get( propertyName );
		if ( columns == null ) {
			throw propertyException( propertyName );
		}
		String[] formulaTemplates = formulaTemplatesByPropertyPath.get( propertyName );
		String[] columnReaderTemplates = columnReaderTemplatesByPropertyPath.get( propertyName );
		String[] result = new String[columns.length];
		for ( int i = 0; i < columns.length; i++ ) {
			if ( columnReaderTemplates[i] == null ) {
				result[i] = StringHelper.replace( formulaTemplates[i], Template.TEMPLATE, alias );
			}
			else {
				result[i] = StringHelper.replace( columnReaderTemplates[i], Template.TEMPLATE, alias );
			}
		}
		return result;
	}

	public String[] toColumns(String propertyName) throws QueryException {
		String[] columns = columnsByPropertyPath.get( propertyName );
		if ( columns == null ) {
			throw propertyException( propertyName );
		}
		String[] formulaTemplates = formulaTemplatesByPropertyPath.get( propertyName );
		String[] columnReaders = columnReadersByPropertyPath.get( propertyName );
		String[] result = new String[columns.length];
		for ( int i = 0; i < columns.length; i++ ) {
			if ( columnReaders[i] == null ) {
				result[i] = StringHelper.replace( formulaTemplates[i], Template.TEMPLATE, "" );
			}
			else {
				result[i] = columnReaders[i];
			}
		}
		return result;
	}

	private void logDuplicateRegistration(String path, Type existingType, Type type) {
		if ( LOG.isTraceEnabled() ) {
			LOG.tracev(
					"Skipping duplicate registration of path [{0}], existing type = [{1}], incoming type = [{2}]",
					path,
					existingType,
					type
			);
		}
	}

	/**
	 * Only kept around for compatibility reasons since this seems to be API.
	 *
	 * @deprecated Use {@link #addPropertyPath(String, Type, String[], String[], String[], String[], Mapping)} instead
	 */
	@Deprecated
	protected void addPropertyPath(
			String path,
			Type type,
			String[] columns,
			String[] columnReaders,
			String[] columnReaderTemplates,
			String[] formulaTemplates) {
		addPropertyPath( path, type, columns, columnReaders, columnReaderTemplates, formulaTemplates, null );
	}

	protected void addPropertyPath(
			String path,
			Type type,
			String[] columns,
			String[] columnReaders,
			String[] columnReaderTemplates,
			String[] formulaTemplates,
			Mapping factory) {
		Type existingType = typesByPropertyPath.get( path );
		if ( existingType != null ) {
			// If types match or the new type is not an association type, there is nothing for us to do
			if ( type == existingType || !( type instanceof AssociationType ) ) {
				logDuplicateRegistration(
						path,
						existingType,
						type
				);
				return;
			}

			// Workaround for org.hibernate.cfg.annotations.PropertyBinder.bind() adding a component for *ToOne ids
			if ( !( existingType instanceof AssociationType ) ) {
				logDuplicateRegistration(
						path,
						existingType,
						type
				);
				return;
			}

			Type newType;
			MetadataImplementor metadata = (MetadataImplementor) factory;

			if ( type instanceof AnyType ) {
				// TODO: not sure how to handle any types
				throw new UnsupportedOperationException( "Not yet implemented!" );
			}
			else if ( type instanceof CollectionType ) {
				Collection thisCollection = metadata.getCollectionBinding( ( (CollectionType) existingType ).getRole() );
				Collection otherCollection = metadata.getCollectionBinding( ( (CollectionType) type ).getRole() );

				if ( thisCollection == null || otherCollection == null ) {
					// This can only happen when we previously replaced a "concrete" type by a more "general" ad-hoc type
					return;
				}
				else if ( thisCollection.isSame( otherCollection ) ) {
					logDuplicateRegistration(
							path,
							existingType,
							type
					);
					return;
				}

				newType = getSuperCollectionType(
						metadata,
						thisCollection,
						otherCollection,
						path
				);
			}
			else if ( type instanceof EntityType ) {
				EntityType entityType1 = (EntityType) existingType;
				EntityType entityType2 = (EntityType) type;

				if ( entityType1.getAssociatedEntityName().equals( entityType2.getAssociatedEntityName() ) ) {
					logDuplicateRegistration(
							path,
							existingType,
							type
					);
					return;
				}

				newType = getCommonType( metadata, entityType1, entityType2 );
			}
			else {
				throw new IllegalStateException( "Unexpected association type: " + type );
			}

			typesByPropertyPath.put( path, newType );
			// Set everything to empty to signal action has to be taken!
			// org.hibernate.hql.internal.ast.tree.DotNode.dereferenceEntityJoin() is reacting to this
			String[] empty = new String[0];
			columnsByPropertyPath.put( path, empty );
			columnReadersByPropertyPath.put( path, empty );
			columnReaderTemplatesByPropertyPath.put( path, empty );
			if ( formulaTemplates != null ) {
				formulaTemplatesByPropertyPath.put( path, empty );
			}
			return;
		}
		typesByPropertyPath.put( path, type );
		columnsByPropertyPath.put( path, columns );
		columnReadersByPropertyPath.put( path, columnReaders );
		columnReaderTemplatesByPropertyPath.put( path, columnReaderTemplates );
		if ( formulaTemplates != null ) {
			formulaTemplatesByPropertyPath.put( path, formulaTemplates );
		}
	}

	private Type getCommonType(MetadataImplementor metadata, EntityType entityType1, EntityType entityType2) {
		PersistentClass thisClass = metadata.getEntityBinding( entityType1.getAssociatedEntityName() );
		PersistentClass otherClass = metadata.getEntityBinding( entityType2.getAssociatedEntityName() );
		PersistentClass commonClass = getCommonPersistentClass( thisClass, otherClass );

		// Create a copy of the type but with the common class
		if ( entityType1 instanceof ManyToOneType ) {
			ManyToOneType t = (ManyToOneType) entityType1;
			return new ManyToOneType( t, commonClass.getEntityName() );
		}
		else if ( entityType1 instanceof SpecialOneToOneType ) {
			SpecialOneToOneType t = (SpecialOneToOneType) entityType1;
			return new SpecialOneToOneType( t, commonClass.getEntityName() );
		}
		else if ( entityType1 instanceof OneToOneType ) {
			OneToOneType t = (OneToOneType) entityType1;
			return new OneToOneType( t, commonClass.getEntityName() );
		}
		else {
			throw new IllegalStateException( "Unexpected entity type: " + entityType1 );
		}
	}

	private PersistentClass getCommonPersistentClass(PersistentClass clazz1, PersistentClass clazz2) {
		while ( clazz2 != null && !clazz2.getMappedClass().isAssignableFrom( clazz1.getMappedClass() ) ) {
			clazz2 = clazz2.getSuperclass();
		}
		return clazz2;
	}

	private CollectionType getSuperCollectionType(MetadataImplementor metadata, Collection collection1, Collection collection2, String propertyName) {
		PersistentClass clazz1 = collection1.getOwner();
		PersistentClass clazz2 = collection2.getOwner();
		PersistentClass commonPersistentClass = getCommonPersistentClass( clazz1, clazz2 );
		PersistentClass persistentClass = commonPersistentClass;

		// First try to find a collection binding for a common super class
		while ( persistentClass != null ) {
			Collection collection = metadata.getCollectionBinding( persistentClass.getEntityName() + "." + propertyName );
			if ( collection != null ) {
				return collection.getCollectionType();
			}

			persistentClass = persistentClass.getSuperclass();
		}

		// TODO: This is ugly, but for now, we can't properly construct
		if ( true ) {
			return null;
		}

		// If we can't find one, we need to build an ad-hoc collection type
		String role = commonPersistentClass.getClassName() + "." + propertyName;
		if ( collection1 instanceof Set ) {
			Set set = (Set) collection1;
			if ( set.isSorted() ) {
				return metadata.getTypeResolver()
						.getTypeFactory()
						.sortedSet( role, propertyName, set.getComparator() );
			}
			else if ( set.hasOrder() ) {
				return metadata.getTypeResolver()
						.getTypeFactory()
						.orderedSet( role, propertyName );
			}
			else {
				return metadata.getTypeResolver()
						.getTypeFactory()
						.set( role, propertyName );
			}
		} else if ( collection1 instanceof Bag ) {
			return metadata.getTypeResolver()
					.getTypeFactory()
					.bag( role, propertyName );
		} else if ( collection1 instanceof org.hibernate.mapping.Map ) {
			org.hibernate.mapping.Map map = (org.hibernate.mapping.Map) collection1;
			if ( map.isSorted() ) {
				return metadata.getTypeResolver()
						.getTypeFactory()
						.sortedMap( role, propertyName, map.getComparator() );
			}
			else if ( map.hasOrder() ) {
				return metadata.getTypeResolver()
						.getTypeFactory()
						.orderedMap( role, propertyName );
			}
			else {
				return metadata.getTypeResolver()
						.getTypeFactory()
						.map( role, propertyName );
			}
		} else if ( collection1 instanceof org.hibernate.mapping.Array ) {
			org.hibernate.mapping.Array array = (org.hibernate.mapping.Array) collection1;
			return metadata.getTypeResolver()
					.getTypeFactory()
					.array( role, propertyName, array.getElementClass() );
		} else if ( collection1 instanceof org.hibernate.mapping.List ) {
			return metadata.getTypeResolver()
					.getTypeFactory()
					.list( role, propertyName );
		} else if ( collection1 instanceof IdentifierBag ) {
			return metadata.getTypeResolver()
					.getTypeFactory()
					.idbag( role, propertyName );
		}

		return null;
	}

	/*protected void initPropertyPaths(
			final String path,
			final Type type,
			final String[] columns,
			final String[] formulaTemplates,
			final Mapping factory)
	throws MappingException {
		//addFormulaPropertyPath(path, type, formulaTemplates);
		initPropertyPaths(path, type, columns, formulaTemplates, factory);
	}*/

	protected void initPropertyPaths(
			final String path,
			final Type type,
			String[] columns,
			String[] columnReaders,
			String[] columnReaderTemplates,
			final String[] formulaTemplates,
			final Mapping factory) throws MappingException {
		assert columns != null : "Incoming columns should not be null : " + path;
		assert type != null : "Incoming type should not be null : " + path;

		if ( columns.length != type.getColumnSpan( factory ) ) {
			throw new MappingException(
					"broken column mapping for: " + path +
							" of: " + getEntityName()
			);
		}

		if ( type.isAssociationType() ) {
			AssociationType actype = (AssociationType) type;
			if ( actype.useLHSPrimaryKey() ) {
				columns = getIdentifierColumnNames();
				columnReaders = getIdentifierColumnReaders();
				columnReaderTemplates = getIdentifierColumnReaderTemplates();
			}
			else {
				String foreignKeyProperty = actype.getLHSPropertyName();
				if ( foreignKeyProperty != null && !path.equals( foreignKeyProperty ) ) {
					//TODO: this requires that the collection is defined after the
					//      referenced property in the mapping file (ok?)
					columns = columnsByPropertyPath.get( foreignKeyProperty );
					if ( columns == null ) {
						return; //get em on the second pass!
					}
					columnReaders = columnReadersByPropertyPath.get( foreignKeyProperty );
					columnReaderTemplates = columnReaderTemplatesByPropertyPath.get( foreignKeyProperty );
				}
			}
		}

		if ( path != null ) {
			addPropertyPath( path, type, columns, columnReaders, columnReaderTemplates, formulaTemplates, factory );
		}

		if ( type.isComponentType() ) {
			CompositeType actype = (CompositeType) type;
			initComponentPropertyPaths(
					path,
					actype,
					columns,
					columnReaders,
					columnReaderTemplates,
					formulaTemplates,
					factory
			);
			if ( actype.isEmbedded() ) {
				initComponentPropertyPaths(
						path == null ? null : StringHelper.qualifier( path ),
						actype,
						columns,
						columnReaders,
						columnReaderTemplates,
						formulaTemplates,
						factory
				);
			}
		}
		else if ( type.isEntityType() ) {
			initIdentifierPropertyPaths(
					path,
					(EntityType) type,
					columns,
					columnReaders,
					columnReaderTemplates,
					factory
			);
		}
	}

	protected void initIdentifierPropertyPaths(
			final String path,
			final EntityType etype,
			final String[] columns,
			final String[] columnReaders,
			final String[] columnReaderTemplates,
			final Mapping factory) throws MappingException {

		Type idtype = etype.getIdentifierOrUniqueKeyType( factory );
		String idPropName = etype.getIdentifierOrUniqueKeyPropertyName( factory );
		boolean hasNonIdentifierPropertyNamedId = hasNonIdentifierPropertyNamedId( etype, factory );

		if ( etype.isReferenceToPrimaryKey() ) {
			if ( !hasNonIdentifierPropertyNamedId ) {
				String idpath1 = extendPath( path, EntityPersister.ENTITY_ID );
				addPropertyPath( idpath1, idtype, columns, columnReaders, columnReaderTemplates, null, factory );
				initPropertyPaths( idpath1, idtype, columns, columnReaders, columnReaderTemplates, null, factory );
			}
		}

		if ( idPropName != null ) {
			String idpath2 = extendPath( path, idPropName );
			addPropertyPath( idpath2, idtype, columns, columnReaders, columnReaderTemplates, null, factory );
			initPropertyPaths( idpath2, idtype, columns, columnReaders, columnReaderTemplates, null, factory );
		}
	}

	private boolean hasNonIdentifierPropertyNamedId(final EntityType entityType, final Mapping factory) {
		// TODO : would be great to have a Mapping#hasNonIdentifierPropertyNamedId method
		// I don't believe that Mapping#getReferencedPropertyType accounts for the identifier property; so
		// if it returns for a property named 'id', then we should have a non-id field named id
		try {
			return factory.getReferencedPropertyType(
					entityType.getAssociatedEntityName(),
					EntityPersister.ENTITY_ID
			) != null;
		}
		catch (MappingException e) {
			return false;
		}
	}

	protected void initComponentPropertyPaths(
			final String path,
			final CompositeType type,
			final String[] columns,
			final String[] columnReaders,
			final String[] columnReaderTemplates,
			String[] formulaTemplates, final Mapping factory) throws MappingException {

		Type[] types = type.getSubtypes();
		String[] properties = type.getPropertyNames();
		int begin = 0;
		for ( int i = 0; i < properties.length; i++ ) {
			String subpath = extendPath( path, properties[i] );
			try {
				int length = types[i].getColumnSpan( factory );
				String[] columnSlice = ArrayHelper.slice( columns, begin, length );
				String[] columnReaderSlice = ArrayHelper.slice( columnReaders, begin, length );
				String[] columnReaderTemplateSlice = ArrayHelper.slice( columnReaderTemplates, begin, length );
				String[] formulaSlice = formulaTemplates == null ?
						null : ArrayHelper.slice( formulaTemplates, begin, length );
				initPropertyPaths(
						subpath,
						types[i],
						columnSlice,
						columnReaderSlice,
						columnReaderTemplateSlice,
						formulaSlice,
						factory
				);
				begin += length;
			}
			catch (Exception e) {
				throw new MappingException( "bug in initComponentPropertyPaths", e );
			}
		}
	}

	private static String extendPath(String path, String property) {
		return StringHelper.isEmpty( path ) ? property : StringHelper.qualify( path, property );
	}
}
