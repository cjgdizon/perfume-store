package com.amira.ms.serviceimpl;

import com.amira.ms.entity.ProductData;
import com.amira.ms.entity.ProductVariantData;
import com.amira.ms.model.BestSeller;
import com.amira.ms.model.CategoryRevenue;
import com.amira.ms.model.InventoryValuation;
import com.amira.ms.model.StockReportLine;
import com.amira.ms.repository.OrderItemDataRepository;
import com.amira.ms.repository.ProductDataRepository;
import com.amira.ms.repository.ProductVariantDataRepository;
import com.amira.ms.service.ReportingService;
import com.amira.ms.service.Rules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ReportingServiceImpl implements ReportingService {
	@Autowired
	OrderItemDataRepository orderItemDataRepository;

	@Autowired
	ProductVariantDataRepository variantDataRepository;

	@Autowired
	ProductDataRepository productDataRepository;

	@Override
	public List<BestSeller> getBestSellers(LocalDate from, LocalDate to) {
		LocalDate end = to != null ? to : LocalDate.now();
		LocalDate start = from != null ? from : end.minusMonths(1);
		List<BestSeller> result = new ArrayList<>();
		for (Object[] row : orderItemDataRepository.bestSellers(start.atStartOfDay(), end.atTime(LocalTime.MAX))) {
			BestSeller seller = new BestSeller();
			seller.setVariantId(((Number) row[0]).intValue());
			seller.setProductName((String) row[1]);
			seller.setSize((String) row[2]);
			seller.setUnitsSold(((Number) row[3]).longValue());
			seller.setRevenue((BigDecimal) row[4]);
			result.add(seller);
		}
		return result;
	}

	@Override
	public List<StockReportLine> getSlowMovingStock(int monthsThreshold) {
		LocalDateTime since = LocalDateTime.now().minusMonths(monthsThreshold);
		Set<Integer> sold = new HashSet<>(orderItemDataRepository.variantIdsSoldSince(since));
		Map<Integer, ProductData> products = products();
		List<StockReportLine> result = new ArrayList<>();
		for (ProductVariantData v : variantDataRepository.findAll()) {
			if (v.getStockQuantity() > 0 && !sold.contains(v.getId())
					&& !Rules.DISCONTINUED.equals(v.getStatus())) {
				result.add(InventoryServiceImpl.toLine(v, products.get(v.getProductId())));
			}
		}
		// biggest tied-up cost first
		result.sort((a, b) -> b.getCostValue().compareTo(a.getCostValue()));
		return result;
	}

	@Override
	public List<CategoryRevenue> getRevenueByCategory() {
		List<CategoryRevenue> result = new ArrayList<>();
		for (Object[] row : orderItemDataRepository.revenueByCategory()) {
			CategoryRevenue revenue = new CategoryRevenue();
			revenue.setCategory((String) row[0]);
			revenue.setRevenue((BigDecimal) row[1]);
			result.add(revenue);
		}
		return result;
	}

	@Override
	public InventoryValuation getInventoryValuation() {
		long units = 0;
		BigDecimal cost = BigDecimal.ZERO;
		BigDecimal retail = BigDecimal.ZERO;
		for (ProductVariantData v : variantDataRepository.findAll()) {
			BigDecimal qty = BigDecimal.valueOf(v.getStockQuantity());
			units += v.getStockQuantity();
			if (v.getCostPrice() != null) {
				cost = cost.add(v.getCostPrice().multiply(qty));
			}
			if (v.getPrice() != null) {
				retail = retail.add(v.getPrice().multiply(qty));
			}
		}
		InventoryValuation valuation = new InventoryValuation();
		valuation.setTotalUnits(units);
		valuation.setCostValue(cost);
		valuation.setRetailValue(retail);
		return valuation;
	}

	private Map<Integer, ProductData> products() {
		Map<Integer, ProductData> products = new HashMap<>();
		productDataRepository.findAll().forEach(p -> products.put(p.getId(), p));
		return products;
	}
}
