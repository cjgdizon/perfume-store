package com.amira.ms.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItem {
	int id;
	int orderId;
	int variantId;
	String productName;
	String size;
	int quantity;
	BigDecimal priceAtPurchase;
	BigDecimal subtotal;
}
