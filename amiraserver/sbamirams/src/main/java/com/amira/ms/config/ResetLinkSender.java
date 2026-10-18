package com.amira.ms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Delivers the password reset link. With spring.mail.host configured it sends an email.
 * Without it (local development) it writes the link to the server log instead.
 */
@Component
public class ResetLinkSender {
	Logger logger = LoggerFactory.getLogger(ResetLinkSender.class);

	@Autowired
	ObjectProvider<JavaMailSender> mailSender;

	@Value("${amira.frontend-url}")
	String frontendUrl;

	@Value("${amira.mail.from}")
	String from;

	public void send(String email, String token) {
		String link = frontendUrl + "/reset-password?token=" + token;
		JavaMailSender sender = mailSender.getIfAvailable();
		if (sender == null) {
			// ponytail: dev fallback only. A link in a log file is a credential, so configure spring.mail.*
			// before this runs anywhere other than a developer machine.
			logger.warn("Mail is not configured. Password reset link for {}: {}", email, link);
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(email);
			message.setSubject("Reset your AMIRA password");
			message.setText("We received a request to reset your AMIRA password.\n\n"
					+ "Open this link within 30 minutes to choose a new one:\n" + link + "\n\n"
					+ "If you did not ask for this, you can ignore this email. Your password has not changed.");
			sender.send(message);
		} catch (Exception e) {
			// never tell the caller: the response must look the same whether or not the email exists
			logger.error("Could not send the password reset email", e);
		}
	}
}
