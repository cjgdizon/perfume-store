package com.amira.ms.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "Payment_data")
public class PaymentData extends AuditData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	int orderId;
	// GCash | Card | Bank Transfer | COD
	String method;
	@Column(precision = 12, scale = 2)
	BigDecimal amount;
	LocalDateTime paymentDate;
	// Pending | Completed | Failed | Refunded
	String status;
}
