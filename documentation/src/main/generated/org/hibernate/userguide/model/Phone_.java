package org.hibernate.userguide.model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Phone.class)
public abstract class Phone_ {

	public static volatile SingularAttribute<Phone, String> number;
	public static volatile ListAttribute<Phone, Call> calls;
	public static volatile SingularAttribute<Phone, Person> person;
	public static volatile SingularAttribute<Phone, Long> id;
	public static volatile SingularAttribute<Phone, PhoneType> type;
	public static volatile MapAttribute<Phone, Date, Call> callHistory;
	public static volatile ListAttribute<Phone, Date> repairTimestamps;

}

