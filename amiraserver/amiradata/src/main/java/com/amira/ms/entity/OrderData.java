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
@Table(name = "Order_data")
public class OrderData extends AuditData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	String description;
	int customerId;
	String customerName;
	String shippingAddress;
	LocalDateTime dateOrdered;
	@Column(precision = 12, scale = 2)
	BigDecimal totalAmount;
	int statusId;
	String statusName;
	// Unpaid | Paid | Refunded
	String paymentStatus;
}
