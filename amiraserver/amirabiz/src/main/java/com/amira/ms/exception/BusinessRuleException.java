package com.amira.ms.exception;

/** A request that is well-formed but breaks a business rule (not enough stock, bad status change...). */
public class BusinessRuleException extends RuntimeException {
	public BusinessRuleException(String message) {
		super(message);
	}
}
