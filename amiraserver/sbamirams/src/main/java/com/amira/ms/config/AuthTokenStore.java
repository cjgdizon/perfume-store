package com.amira.ms.config;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ponytail: tokens live in memory, so everyone signs in again after a server restart and it
 * only works on a single instance. Move to signed JWTs if this ever runs on more than one node.
 */
@Component
public class AuthTokenStore {
	private static final Duration LIFETIME = Duration.ofHours(8);

	private record Session(AuthUser user, Instant expires) {
	}

	private final Map<String, Session> sessions = new ConcurrentHashMap<>();

	public String issue(AuthUser user) {
		String token = UUID.randomUUID().toString();
		sessions.put(token, new Session(user, Instant.now().plus(LIFETIME)));
		return token;
	}

	public AuthUser find(String token) {
		Session session = sessions.get(token);
		if (session == null) {
			return null;
		}
		if (session.expires().isBefore(Instant.now())) {
			sessions.remove(token);
			return null;
		}
		return session.user();
	}

	/** Ends every open session of one customer, e.g. after their password was reset. */
	public void revokeAll(int customerId) {
		sessions.values().removeIf(s -> s.user().customerId() == customerId);
	}

	public void revoke(String token) {
		sessions.remove(token);
	}
}
