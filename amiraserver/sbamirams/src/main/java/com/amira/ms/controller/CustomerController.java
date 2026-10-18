package com.amira.ms.controller;

import com.amira.ms.config.AuthContext;
import com.amira.ms.config.AuthTokenStore;
import com.amira.ms.config.AuthUser;
import com.amira.ms.config.ResetLinkSender;
import com.amira.ms.model.Customer;
import com.amira.ms.model.LoginRequest;
import com.amira.ms.model.Order;
import com.amira.ms.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class CustomerController {
	public record LoginResponse(String token, Customer customer) {
	}

	public record ForgotPasswordRequest(String email) {
	}

	public record ResetPasswordRequest(String token, String password) {
	}

	@Autowired
	private CustomerService customerService;

	@Autowired
	private AuthTokenStore tokenStore;

	@Autowired
	private ResetLinkSender resetLinkSender;

	@PostMapping("/api/customer/register")
	public LoginResponse register(@RequestBody Customer customer) {
		Customer created = customerService.registerCustomer(customer);
		return signIn(created);
	}

	@PostMapping("/api/customer/login")
	public LoginResponse login(@RequestBody LoginRequest login) {
		return signIn(customerService.authenticateCustomer(login.getEmail(), login.getPassword()));
	}

	// always the same answer, so the form cannot be used to find out which emails are registered
	@PostMapping("/api/customer/forgot-password")
	public Map<String, String> forgotPassword(@RequestBody ForgotPasswordRequest body) {
		customerService.requestPasswordReset(body.email())
				.ifPresent(token -> resetLinkSender.send(body.email().trim(), token));
		return Map.of("message", "If that email is registered, we have sent a link to reset the password.");
	}

	@PostMapping("/api/customer/reset-password")
	public Map<String, String> resetPassword(@RequestBody ResetPasswordRequest body) {
		Customer customer = customerService.resetPassword(body.token(), body.password());
		tokenStore.revokeAll(customer.getId());
		return Map.of("message", "Your password has been changed. Please sign in.");
	}

	@PostMapping("/api/customer/logout")
	public void logout(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			tokenStore.revoke(header.substring(7));
		}
	}

	@GetMapping("/api/customer")
	public List<Customer> list() {
		return customerService.getAll();
	}

	@GetMapping("/api/customer/{id}")
	public Customer get(@PathVariable int id, HttpServletRequest request) {
		AuthContext.requireSelfOrOwner(request, id);
		return customerService.get(id);
	}

	@GetMapping("/api/customer/{id}/orders")
	public List<Order> orders(@PathVariable int id, HttpServletRequest request) {
		AuthContext.requireSelfOrOwner(request, id);
		return customerService.getCustomerOrders(id);
	}

	@PutMapping("/api/customer")
	public Customer update(@RequestBody Customer customer, HttpServletRequest request) {
		AuthContext.requireSelfOrOwner(request, customer.getId());
		return customerService.updateProfile(customer);
	}

	@DeleteMapping("/api/customer/{id}")
	public void delete(@PathVariable int id) {
		customerService.delete(id);
	}

	private LoginResponse signIn(Customer customer) {
		String token = tokenStore.issue(new AuthUser(customer.getId(), customer.getName(), customer.getRole()));
		return new LoginResponse(token, customer);
	}
}
