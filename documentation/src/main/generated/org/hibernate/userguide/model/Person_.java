package org.hibernate.userguide.model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Person.class)
public abstract class Person_ {

	public static volatile MapAttribute<Person, AddressType, String> addresses;
	public static volatile SingularAttribute<Person, String> address;
	public static volatile SingularAttribute<Person, String> nickName;
	public static volatile SingularAttribute<Person, String> name;
	public static volatile ListAttribute<Person, Phone> phones;
	public static volatile SingularAttribute<Person, Long> id;
	public static volatile SingularAttribute<Person, Date> createdOn;
	public static volatile SingularAttribute<Person, Integer> version;

}

