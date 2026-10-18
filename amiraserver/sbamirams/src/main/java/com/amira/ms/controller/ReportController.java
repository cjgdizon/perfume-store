package com.amira.ms.controller;

import com.amira.ms.model.BestSeller;
import com.amira.ms.model.CategoryRevenue;
import com.amira.ms.model.InventoryValuation;
import com.amira.ms.model.StockReportLine;
import com.amira.ms.service.InventoryService;
import com.amira.ms.service.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ReportController {
	@Autowired
	private ReportingService reportingService;

	@Autowired
	private InventoryService inventoryService;

	@GetMapping("/api/report/stock")
	public List<StockReportLine> stock() {
		return inventoryService.generateStockReport();
	}

	@GetMapping("/api/report/best-sellers")
	public List<BestSeller> bestSellers(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return reportingService.getBestSellers(from, to);
	}

	@GetMapping("/api/report/slow-moving")
	public List<StockReportLine> slowMoving(@RequestParam(defaultValue = "6") int months) {
		return reportingService.getSlowMovingStock(months);
	}

	@GetMapping("/api/report/revenue-by-category")
	public List<CategoryRevenue> revenueByCategory() {
		return reportingService.getRevenueByCategory();
	}

	@GetMapping("/api/report/valuation")
	public InventoryValuation valuation() {
		return reportingService.getInventoryValuation();
	}
}
