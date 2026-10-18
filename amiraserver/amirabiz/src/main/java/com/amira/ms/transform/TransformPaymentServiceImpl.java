package com.amira.ms.transform;

import com.amira.ms.entity.PaymentData;
import com.amira.ms.model.Payment;
import org.springframework.stereotype.Service;

@Service
public class TransformPaymentServiceImpl implements TransformPaymentService {
	@Override
	public PaymentData transform(Payment payment) {
		PaymentData data = new PaymentData();
		data.setId(payment.getId());
		data.setOrderId(payment.getOrderId());
		data.setMethod(payment.getMethod());
		data.setAmount(payment.getAmount());
		data.setPaymentDate(payment.getPaymentDate());
		data.setStatus(payment.getStatus());
		return data;
	}

	@Override
	public Payment transform(PaymentData data) {
		Payment payment = new Payment();
		payment.setId(data.getId());
		payment.setOrderId(data.getOrderId());
		payment.setMethod(data.getMethod());
		payment.setAmount(data.getAmount());
		payment.setPaymentDate(data.getPaymentDate());
		payment.setStatus(data.getStatus());
		return payment;
	}
}
