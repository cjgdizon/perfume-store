package com.amira.ms.service;

import com.amira.ms.model.Customer;
import com.amira.ms.model.Order;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
	List<Customer> getAll();

	Customer get(int id);

	Customer registerCustomer(Customer customer);

	Customer authenticateCustomer(String email, String password);

	Customer updateProfile(Customer customer);

	// empty when the email is not registered; otherwise the one-time token to put in the reset link
	Optional<String> requestPasswordReset(String email);

	// returns the customer whose password changed, so their open sessions can be ended
	Customer resetPassword(String token, String newPassword);

	void delete(int id);

	List<Order> getCustomerOrders(int customerId);
}
