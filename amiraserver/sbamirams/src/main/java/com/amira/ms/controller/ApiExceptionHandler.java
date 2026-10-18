package com.amira.ms.controller;

import com.amira.ms.exception.BusinessRuleException;
import com.amira.ms.exception.ForbiddenException;
import com.amira.ms.exception.NotFoundException;
import com.amira.ms.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/** One place that turns exceptions into {"message": "..."} with a meaningful status. */
@RestControllerAdvice
public class ApiExceptionHandler {
	Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Map<String, String>> notFound(NotFoundException ex) {
		return body(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<Map<String, String>> businessRule(BusinessRuleException ex) {
		return body(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<Map<String, String>> unauthorized(UnauthorizedException ex) {
		return body(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<Map<String, String>> forbidden(ForbiddenException ex) {
		return body(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	public ResponseEntity<Map<String, String>> badRequest(Exception ex) {
		return body(HttpStatus.BAD_REQUEST, "The request could not be read. Check the values you sent.");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> unexpected(Exception ex) {
		logger.error("Unhandled error", ex);
		return body(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on the server.");
	}

	private static ResponseEntity<Map<String, String>> body(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of("message", message == null ? "" : message));
	}
}
