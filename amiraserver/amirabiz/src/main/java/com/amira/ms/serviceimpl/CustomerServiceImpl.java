package com.amira.ms.serviceimpl;

import com.amira.ms.entity.CustomerData;
import com.amira.ms.entity.PasswordResetData;
import com.amira.ms.exception.BusinessRuleException;
import com.amira.ms.exception.NotFoundException;
import com.amira.ms.exception.UnauthorizedException;
import com.amira.ms.model.Customer;
import com.amira.ms.model.Order;
import com.amira.ms.repository.CustomerDataRepository;
import com.amira.ms.repository.PasswordResetDataRepository;
import com.amira.ms.service.CustomerService;
import com.amira.ms.service.OrderService;
import com.amira.ms.service.Rules;
import com.amira.ms.transform.TransformCustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {
	Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

	private static final int MIN_PASSWORD = 6;
	private static final Duration RESET_LIFETIME = Duration.ofMinutes(30);

	private final SecureRandom random = new SecureRandom();

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Autowired
	CustomerDataRepository customerDataRepository;

	@Autowired
	PasswordResetDataRepository passwordResetDataRepository;

	@Autowired
	TransformCustomerService transformCustomerService;

	@Autowired
	OrderService orderService;

	@Override
	@Transactional(readOnly = true)
	public List<Customer> getAll() {
		return StreamSupport.stream(customerDataRepository.findAll().spliterator(), false)
				.map(transformCustomerService::transform).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Customer get(int id) {
		return transformCustomerService.transform(find(id));
	}

	@Override
	public Customer registerCustomer(Customer customer) {
		String email = customer.getEmail() == null ? "" : customer.getEmail().trim().toLowerCase();
		if (customer.getName() == null || customer.getName().isBlank()) {
			throw new BusinessRuleException("Name is required");
		}
		if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			throw new BusinessRuleException("A valid email is required");
		}
		requirePassword(customer.getPassword());
		if (customerDataRepository.findByEmailIgnoreCase(email).isPresent()) {
			throw new BusinessRuleException("That email is already registered");
		}
		CustomerData data = transformCustomerService.transform(customer);
		data.setId(0);
		data.setEmail(email);
		data.setRole(Rules.CUSTOMER);
		data.setPasswordHash(encoder.encode(customer.getPassword()));
		data = customerDataRepository.save(data);
		logger.info("registered customer {}", data);
		return transformCustomerService.transform(data);
	}

	@Override
	@Transactional(readOnly = true)
	public Customer authenticateCustomer(String email, String password) {
		CustomerData data = customerDataRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
				.orElse(null);
		// same message for unknown email and wrong password
		if (data == null || password == null || !encoder.matches(password, data.getPasswordHash())) {
			throw new UnauthorizedException("Invalid email or password");
		}
		return transformCustomerService.transform(data);
	}

	@Override
	public Customer updateProfile(Customer customer) {
		CustomerData data = find(customer.getId());
		if (customer.getName() == null || customer.getName().isBlank()) {
			throw new BusinessRuleException("Name is required");
		}
		data.setName(customer.getName());
		data.setPhone(customer.getPhone());
		data.setShippingAddress(customer.getShippingAddress());
		if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
			requirePassword(customer.getPassword());
			data.setPasswordHash(encoder.encode(customer.getPassword()));
		}
		return transformCustomerService.transform(customerDataRepository.save(data));
	}

	@Override
	public Optional<String> requestPasswordReset(String email) {
		Optional<CustomerData> found = customerDataRepository.findByEmailIgnoreCase(email == null ? "" : email.trim());
		if (found.isEmpty()) {
			return Optional.empty();
		}
		CustomerData customer = found.get();
		// one live link per customer: a new request cancels the previous one
		passwordResetDataRepository.deleteByCustomerId(customer.getId());
		byte[] raw = new byte[32];
		random.nextBytes(raw);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
		PasswordResetData reset = new PasswordResetData();
		reset.setCustomerId(customer.getId());
		reset.setTokenHash(sha256(token));
		reset.setExpiresAt(LocalDateTime.now().plus(RESET_LIFETIME));
		passwordResetDataRepository.save(reset);
		return Optional.of(token);
	}

	@Override
	public Customer resetPassword(String token, String newPassword) {
		requirePassword(newPassword);
		PasswordResetData reset = token == null ? null
				: passwordResetDataRepository.findByTokenHash(sha256(token)).orElse(null);
		// unknown, used and expired links get the same answer
		if (reset == null || reset.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new BusinessRuleException("This reset link is invalid or has expired. Request a new one.");
		}
		CustomerData customer = find(reset.getCustomerId());
		customer.setPasswordHash(encoder.encode(newPassword));
		customerDataRepository.save(customer);
		// the link works once
		passwordResetDataRepository.deleteByCustomerId(customer.getId());
		return transformCustomerService.transform(customer);
	}

	private static String sha256(String value) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void delete(int id) {
		customerDataRepository.delete(find(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Order> getCustomerOrders(int customerId) {
		find(customerId);
		return orderService.getOrderHistory(customerId);
	}

	private CustomerData find(int id) {
		return customerDataRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Customer " + id + " not found"));
	}

	private static void requirePassword(String password) {
		if (password == null || password.length() < MIN_PASSWORD) {
			throw new BusinessRuleException("Password must be at least " + MIN_PASSWORD + " characters");
		}
	}
}
