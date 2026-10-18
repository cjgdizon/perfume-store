package com.amira.ms.service;

import com.amira.ms.model.Payment;

import java.util.List;

public interface PaymentService {
	Payment get(int id);

	List<Payment> listByOrderId(int orderId);

	Payment processPayment(int orderId, String method);

	Payment confirmPayment(int paymentId);

	Payment refundPayment(int paymentId);
}
