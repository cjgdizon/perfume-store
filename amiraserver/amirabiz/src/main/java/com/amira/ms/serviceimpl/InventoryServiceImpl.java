package com.amira.ms.serviceimpl;

import com.amira.ms.entity.ProductData;
import com.amira.ms.entity.ProductVariantData;
import com.amira.ms.exception.BusinessRuleException;
import com.amira.ms.exception.NotFoundException;
import com.amira.ms.model.StockReportLine;
import com.amira.ms.repository.ProductDataRepository;
import com.amira.ms.repository.ProductVariantDataRepository;
import com.amira.ms.service.InventoryService;
import com.amira.ms.service.Rules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {
	@Autowired
	ProductVariantDataRepository variantDataRepository;

	@Autowired
	ProductDataRepository productDataRepository;

	@Override
	@Transactional(readOnly = true)
	public boolean checkAvailability(int variantId, int quantity) {
		return quantity > 0 && variantDataRepository.findById(variantId)
				.map(v -> Rules.sellable(v) && Rules.available(v) >= quantity).orElse(false);
	}

	@Override
	public void reserveStock(int variantId, int quantity) {
		ProductVariantData v = lock(variantId);
		// re-checked under the row lock so two buyers cannot both take the last bottle
		if (quantity <= 0 || !Rules.sellable(v) || Rules.available(v) < quantity) {
			throw new BusinessRuleException("Not enough stock available for " + describe(v));
		}
		v.setReservedQuantity(v.getReservedQuantity() + quantity);
		variantDataRepository.save(v);
	}

	@Override
	public void deductStock(int variantId, int quantity) {
		ProductVariantData v = lock(variantId);
		if (v.getReservedQuantity() < quantity || v.getStockQuantity() < quantity) {
			throw new BusinessRuleException("Reserved stock is missing for " + describe(v));
		}
		v.setStockQuantity(v.getStockQuantity() - quantity);
		v.setReservedQuantity(v.getReservedQuantity() - quantity);
		variantDataRepository.save(v);
	}

	@Override
	public void restoreStock(int variantId, int quantity) {
		ProductVariantData v = lock(variantId);
		v.setStockQuantity(v.getStockQuantity() + quantity);
		variantDataRepository.save(v);
	}

	@Override
	public void releaseReservation(int variantId, int quantity) {
		ProductVariantData v = lock(variantId);
		v.setReservedQuantity(Math.max(0, v.getReservedQuantity() - quantity));
		variantDataRepository.save(v);
	}

	@Override
	@Transactional(readOnly = true)
	public List<StockReportLine> generateStockReport() {
		Map<Integer, ProductData> products = new HashMap<>();
		productDataRepository.findAll().forEach(p -> products.put(p.getId(), p));
		List<StockReportLine> lines = new ArrayList<>();
		for (ProductVariantData v : variantDataRepository.findAll()) {
			lines.add(toLine(v, products.get(v.getProductId())));
		}
		lines.sort((a, b) -> a.getProductName().compareToIgnoreCase(b.getProductName()));
		return lines;
	}

	/** Shared with ReportingService so both reports describe a variant the same way. */
	public static StockReportLine toLine(ProductVariantData v, ProductData p) {
		StockReportLine line = new StockReportLine();
		line.setVariantId(v.getId());
		line.setProductName(p != null ? p.getName() : "");
		line.setBrand(p != null ? p.getBrand() : "");
		line.setCategory(p != null ? p.getCategory() : "");
		line.setSize(v.getSize());
		line.setSku(v.getSku());
		line.setBatchNumber(v.getBatchNumber());
		line.setExpiryDate(v.getExpiryDate());
		line.setStockQuantity(v.getStockQuantity());
		line.setReservedQuantity(v.getReservedQuantity());
		line.setAvailableQuantity(Rules.available(v));
		line.setStatus(Rules.variantStatus(v));
		BigDecimal cost = v.getCostPrice() != null ? v.getCostPrice() : BigDecimal.ZERO;
		line.setCostValue(cost.multiply(BigDecimal.valueOf(v.getStockQuantity())));
		return line;
	}

	private ProductVariantData lock(int variantId) {
		return variantDataRepository.findByIdForUpdate(variantId)
				.orElseThrow(() -> new NotFoundException("Variant " + variantId + " not found"));
	}

	private String describe(ProductVariantData v) {
		String name = productDataRepository.findById(v.getProductId()).map(ProductData::getName).orElse("variant");
		return name + " " + v.getSize();
	}
}
