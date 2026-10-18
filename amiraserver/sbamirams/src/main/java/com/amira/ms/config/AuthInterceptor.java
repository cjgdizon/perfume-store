package com.amira.ms.config;

import com.amira.ms.exception.ForbiddenException;
import com.amira.ms.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Three access levels: public (browse the catalog, register, sign in), signed-in (orders,
 * payments, own profile) and owner (catalog edits, stock, reports, order fulfilment).
 * Per-record checks (a customer reading only their own order) live in the controllers.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
	@Autowired
	AuthTokenStore tokenStore;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String method = request.getMethod();
		String path = request.getRequestURI();
		if ("OPTIONS".equals(method)) {
			return true;
		}

		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			AuthUser user = tokenStore.find(header.substring(7));
			if (user != null) {
				request.setAttribute(AuthContext.ATTRIBUTE, user);
			}
		}

		if (isPublic(method, path)) {
			return true;
		}
		AuthUser user = AuthContext.user(request);
		if (user == null) {
			throw new UnauthorizedException("Please sign in");
		}
		if (requiresOwner(method, path) && !user.isOwner()) {
			throw new ForbiddenException("Owner access required");
		}
		return true;
	}

	private static boolean isOwnerVariantList(String path) {
		return path.equals("/api/variant/expiring") || path.equals("/api/variant/low-stock");
	}

	private static boolean isPublic(String method, String path) {
		if ("POST".equals(method) && (path.equals("/api/customer/register") || path.equals("/api/customer/login")
				|| path.equals("/api/customer/forgot-password") || path.equals("/api/customer/reset-password"))) {
			return true;
		}
		if (!"GET".equals(method)) {
			return false;
		}
		return path.equals("/api/status") || path.startsWith("/api/product")
				|| (path.startsWith("/api/variant") && !isOwnerVariantList(path));
	}

	private static boolean requiresOwner(String method, String path) {
		boolean write = !"GET".equals(method);
		if (path.startsWith("/api/report") || isOwnerVariantList(path)) {
			return true;
		}
		if (write && (path.startsWith("/api/product") || path.startsWith("/api/variant")
				|| path.startsWith("/api/status"))) {
			return true;
		}
		if ("GET".equals(method) && (path.equals("/api/customer") || path.equals("/api/order"))) {
			return true;
		}
		if ("DELETE".equals(method) && path.startsWith("/api/customer")) {
			return true;
		}
		if (path.matches("/api/payment/\\d+/(confirm|refund)")) {
			return true;
		}
		return "PUT".equals(method) && path.matches("/api/order/\\d+/status");
	}
}
