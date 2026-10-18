package com.amira.ms.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@Entity
@Table(name = "ProductVariant_data")
public class ProductVariantData extends AuditData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	int productId;
	String size;
	String sku;
	@Column(precision = 12, scale = 2)
	BigDecimal price;
	@Column(precision = 12, scale = 2)
	BigDecimal costPrice;
	int stockQuantity;
	int reservedQuantity;
	String batchNumber;
	LocalDate manufactureDate;
	LocalDate expiryDate;
	// stored: Active | Discontinued. LowStock and Expired are derived on read.
	String status;
}
