package com.amira.ms.service;

import com.amira.ms.entity.ProductVariantData;

import java.time.LocalDate;

/** Constants and small pure rules shared by the services and transformers. */
public final class Rules {
	private Rules() {
	}

	public static final int LOW_STOCK_THRESHOLD = 5;

	public static final String ACTIVE = "Active";
	public static final String LOW_STOCK = "LowStock";
	public static final String EXPIRED = "Expired";
	public static final String DISCONTINUED = "Discontinued";

	public static final String PENDING = "Pending";
	public static final String PAID = "Paid";
	public static final String SHIPPED = "Shipped";
	public static final String DELIVERED = "Delivered";
	public static final String CANCELLED = "Cancelled";

	public static final String UNPAID = "Unpaid";
	public static final String REFUNDED = "Refunded";

	public static final String PAYMENT_PENDING = "Pending";
	public static final String PAYMENT_COMPLETED = "Completed";
	public static final String PAYMENT_FAILED = "Failed";

	public static final String CUSTOMER = "CUSTOMER";
	public static final String OWNER = "OWNER";

	public static int available(ProductVariantData v) {
		return v.getStockQuantity() - v.getReservedQuantity();
	}

	public static String variantStatus(ProductVariantData v) {
		if (DISCONTINUED.equals(v.getStatus())) {
			return DISCONTINUED;
		}
		if (v.getExpiryDate() != null && v.getExpiryDate().isBefore(LocalDate.now())) {
			return EXPIRED;
		}
		return available(v) <= LOW_STOCK_THRESHOLD ? LOW_STOCK : ACTIVE;
	}

	/** Only Active and LowStock variants may be sold. */
	public static boolean sellable(ProductVariantData v) {
		String status = variantStatus(v);
		return ACTIVE.equals(status) || LOW_STOCK.equals(status);
	}
}
