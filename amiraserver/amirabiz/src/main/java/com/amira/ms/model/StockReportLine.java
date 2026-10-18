package com.amira.ms.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockReportLine {
	int variantId;
	String productName;
	String brand;
	String category;
	String size;
	String sku;
	String batchNumber;
	LocalDate expiryDate;
	int stockQuantity;
	int reservedQuantity;
	int availableQuantity;
	String status;
	BigDecimal costValue;
}
