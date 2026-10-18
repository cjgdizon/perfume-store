package com.amira.ms.service;

import com.amira.ms.model.BestSeller;
import com.amira.ms.model.CategoryRevenue;
import com.amira.ms.model.InventoryValuation;
import com.amira.ms.model.StockReportLine;

import java.time.LocalDate;
import java.util.List;

public interface ReportingService {
	List<BestSeller> getBestSellers(LocalDate from, LocalDate to);

	// variants with stock on hand that have not sold in the last N months
	List<StockReportLine> getSlowMovingStock(int monthsThreshold);

	List<CategoryRevenue> getRevenueByCategory();

	InventoryValuation getInventoryValuation();
}
