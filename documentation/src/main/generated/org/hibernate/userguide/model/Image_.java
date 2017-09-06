package org.hibernate.userguide.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Image.class)
public abstract class Image_ {

	public static volatile SingularAttribute<Image, Long> id;
	public static volatile SingularAttribute<Image, byte[]> content;

}

