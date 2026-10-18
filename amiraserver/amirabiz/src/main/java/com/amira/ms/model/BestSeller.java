package com.amira.ms.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BestSeller {
	int variantId;
	String productName;
	String size;
	long unitsSold;
	BigDecimal revenue;
}
