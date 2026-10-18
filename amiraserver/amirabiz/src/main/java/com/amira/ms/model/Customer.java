package com.amira.ms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password")
public class Customer {
	int id;
	String name;
	String email;
	String phone;
	String shippingAddress;
	// CUSTOMER | OWNER
	String role;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	String password;
}
