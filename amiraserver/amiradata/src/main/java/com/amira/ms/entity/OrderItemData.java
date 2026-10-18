package com.amira.ms.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Entity
@Table(name = "OrderItem_data")
public class OrderItemData extends AuditData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	int orderId;
	int variantId;
	String productName;
	String size;
	int quantity;
	@Column(precision = 12, scale = 2)
	BigDecimal priceAtPurchase;
	@Column(precision = 12, scale = 2)
	BigDecimal subtotal;
}
