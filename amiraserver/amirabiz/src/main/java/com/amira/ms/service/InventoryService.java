package com.amira.ms.service;

import com.amira.ms.model.StockReportLine;

import java.util.List;

/**
 * Stock rules. stockQuantity is what is physically on hand, reservedQuantity is held for
 * unpaid orders, available = stock - reserved.
 */
public interface InventoryService {
	boolean checkAvailability(int variantId, int quantity);

	// order placed: hold stock
	void reserveStock(int variantId, int quantity);

	// payment confirmed: stock leaves the shelf
	void deductStock(int variantId, int quantity);

	// paid order cancelled: goods go back on hand
	void restoreStock(int variantId, int quantity);

	// unpaid order cancelled: drop the hold
	void releaseReservation(int variantId, int quantity);

	List<StockReportLine> generateStockReport();
}
