package com.amira.ms.controller;

import com.amira.ms.config.AuthContext;
import com.amira.ms.config.AuthUser;
import com.amira.ms.exception.BusinessRuleException;
import com.amira.ms.model.Order;
import com.amira.ms.service.OrderService;
import com.amira.ms.service.Rules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {
	Logger logger = LoggerFactory.getLogger(OrderController.class);

	@Autowired
	private OrderService orderService;

	@GetMapping("/api/order")
	public List<Order> listAll() {
		return orderService.getAll();
	}

	@GetMapping("/api/order/customer/{customerId}")
	public List<Order> history(@PathVariable int customerId, HttpServletRequest request) {
		AuthContext.requireSelfOrOwner(request, customerId);
		return orderService.getOrderHistory(customerId);
	}

	@GetMapping("/api/order/{id}")
	public Order get(@PathVariable int id, HttpServletRequest request) {
		return ownOrder(id, request);
	}

	@PostMapping("/api/order")
	public Order add(@RequestBody Order order, HttpServletRequest request) {
		AuthUser user = AuthContext.require(request);
		// a shopper can only order for themselves; the owner may place an order for a customer
		if (!user.isOwner() || order.getCustomerId() == 0) {
			order.setCustomerId(user.customerId());
		}
		logger.info("Input >> {}", order);
		return orderService.create(order);
	}

	// owner moves orders along: Shipped, Delivered (or Cancelled)
	@PutMapping("/api/order/{id}/status")
	public Order updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
		String status = body.get("status");
		if (!Rules.SHIPPED.equals(status) && !Rules.DELIVERED.equals(status) && !Rules.CANCELLED.equals(status)) {
			throw new BusinessRuleException("Status must be Shipped, Delivered or Cancelled. Paid is set by payment.");
		}
		return orderService.updateOrderStatus(id, status);
	}

	@PostMapping("/api/order/{id}/cancel")
	public Order cancel(@PathVariable int id, HttpServletRequest request) {
		ownOrder(id, request);
		return orderService.cancelOrder(id);
	}

	/** Loads an order and makes sure the caller is its customer or the owner. */
	private Order ownOrder(int id, HttpServletRequest request) {
		Order order = orderService.get(id);
		AuthContext.requireSelfOrOwner(request, order.getCustomerId());
		return order;
	}
}
