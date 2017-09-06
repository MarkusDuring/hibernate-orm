package org.hibernate.userguide.model;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Payment.class)
public abstract class Payment_ {

	public static volatile SingularAttribute<Payment, BigDecimal> amount;
	public static volatile SingularAttribute<Payment, Person> person;
	public static volatile SingularAttribute<Payment, Long> id;
	public static volatile SingularAttribute<Payment, Boolean> completed;

}

