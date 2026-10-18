package com.amira.ms.controller;

import com.amira.ms.config.AuthContext;
import com.amira.ms.model.Order;
import com.amira.ms.model.Payment;
import com.amira.ms.service.OrderService;
import com.amira.ms.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class PaymentController {
	public record PaymentRequest(int orderId, String method) {
	}

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private OrderService orderService;

	@PostMapping("/api/payment")
	public Payment pay(@RequestBody PaymentRequest body, HttpServletRequest request) {
		requireOrderAccess(body.orderId(), request);
		return paymentService.processPayment(body.orderId(), body.method());
	}

	@GetMapping("/api/payment/order/{orderId}")
	public List<Payment> byOrder(@PathVariable int orderId, HttpServletRequest request) {
		requireOrderAccess(orderId, request);
		return paymentService.listByOrderId(orderId);
	}

	@PutMapping("/api/payment/{id}/confirm")
	public Payment confirm(@PathVariable int id) {
		return paymentService.confirmPayment(id);
	}

	@PostMapping("/api/payment/{id}/refund")
	public Payment refund(@PathVariable int id) {
		return paymentService.refundPayment(id);
	}

	private void requireOrderAccess(int orderId, HttpServletRequest request) {
		Order order = orderService.get(orderId);
		AuthContext.requireSelfOrOwner(request, order.getCustomerId());
	}
}
