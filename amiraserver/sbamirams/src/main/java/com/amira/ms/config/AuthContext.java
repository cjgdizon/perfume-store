package com.amira.ms.config;

import com.amira.ms.exception.ForbiddenException;
import com.amira.ms.exception.UnauthorizedException;

import javax.servlet.http.HttpServletRequest;

/** Reads the signed-in user that AuthInterceptor attached to the request. */
public final class AuthContext {
	static final String ATTRIBUTE = "authUser";

	private AuthContext() {
	}

	/** The signed-in user, or null for an anonymous request. */
	public static AuthUser user(HttpServletRequest request) {
		return (AuthUser) request.getAttribute(ATTRIBUTE);
	}

	public static AuthUser require(HttpServletRequest request) {
		AuthUser user = user(request);
		if (user == null) {
			throw new UnauthorizedException("Please sign in");
		}
		return user;
	}

	public static boolean isOwner(HttpServletRequest request) {
		AuthUser user = user(request);
		return user != null && user.isOwner();
	}

	public static void requireSelfOrOwner(HttpServletRequest request, int customerId) {
		AuthUser user = require(request);
		if (!user.isOwner() && user.customerId() != customerId) {
			throw new ForbiddenException("You can only access your own records");
		}
	}
}
