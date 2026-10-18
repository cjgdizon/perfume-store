package com.amira.ms.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password")
public class LoginRequest {
	String email;
	String password;
}
