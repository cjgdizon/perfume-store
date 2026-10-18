package com.amira.ms.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductVariant {
	int id;
	int productId;
	String productName;
	String size;
	String sku;
	BigDecimal price;
	BigDecimal costPrice;
	int stockQuantity;
	int reservedQuantity;
	int availableQuantity;
	String batchNumber;
	LocalDate manufactureDate;
	LocalDate expiryDate;
	// Active | LowStock | Expired | Discontinued
	String status;
}
