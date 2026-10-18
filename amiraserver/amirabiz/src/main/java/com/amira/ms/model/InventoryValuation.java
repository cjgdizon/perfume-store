package com.amira.ms.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryValuation {
	long totalUnits;
	// units on hand x costPrice
	BigDecimal costValue;
	// units on hand x selling price
	BigDecimal retailValue;
}
