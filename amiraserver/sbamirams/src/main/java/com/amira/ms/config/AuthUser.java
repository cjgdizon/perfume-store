package com.amira.ms.config;

public record AuthUser(int customerId, String name, String role) {
	public boolean isOwner() {
		return "OWNER".equals(role);
	}
}
