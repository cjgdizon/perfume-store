package com.amira.ms.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Order {
	int id;
	String description;
	int customerId;
	String customerName;
	String shippingAddress;
	LocalDateTime dateOrdered;
	BigDecimal totalAmount;
	int statusId;
	// Pending | Paid | Shipped | Delivered | Cancelled
	String statusName;
	// Unpaid | Paid | Refunded
	String paymentStatus;
	List<OrderItem> items = new ArrayList<>();
}
