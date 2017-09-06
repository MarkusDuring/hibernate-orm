package org.hibernate.userguide.model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Call.class)
public abstract class Call_ {

	public static volatile SingularAttribute<Call, Integer> duration;
	public static volatile SingularAttribute<Call, Phone> phone;
	public static volatile SingularAttribute<Call, Long> id;
	public static volatile SingularAttribute<Call, Date> timestamp;

}

