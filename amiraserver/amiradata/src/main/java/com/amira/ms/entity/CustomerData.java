package com.amira.ms.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString(exclude = "passwordHash")
@Entity
@Table(name = "Customer_data", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class CustomerData extends AuditData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	String name;
	String email;
	String phone;
	String shippingAddress;
	String passwordHash;
	// CUSTOMER | OWNER
	String role;
}
