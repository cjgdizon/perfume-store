package com.amira.ms.transform;

import com.amira.ms.entity.PaymentData;
import com.amira.ms.model.Payment;

public interface TransformPaymentService {
	PaymentData transform(Payment payment);

	Payment transform(PaymentData paymentData);
}
