package com.amira.ms.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Payment {
	int id;
	int orderId;
	// GCash | Card | Bank Transfer | COD
	String method;
	BigDecimal amount;
	LocalDateTime paymentDate;
	// Pending | Completed | Failed | Refunded
	String status;
}
