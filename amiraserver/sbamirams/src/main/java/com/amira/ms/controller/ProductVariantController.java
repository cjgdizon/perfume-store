package com.amira.ms.controller;

import com.amira.ms.config.AuthContext;
import com.amira.ms.model.ProductVariant;
import com.amira.ms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class ProductVariantController {
	@Autowired
	private ProductService productService;

	@GetMapping("/api/variant/{id}")
	public ProductVariant get(@PathVariable int id, HttpServletRequest request) {
		ProductVariant variant = productService.getVariant(id);
		if (!AuthContext.isOwner(request)) {
			variant.setCostPrice(null);
		}
		return variant;
	}

	@GetMapping("/api/variant/expiring")
	public List<ProductVariant> expiring(@RequestParam(defaultValue = "90") int days) {
		return productService.getExpiringVariants(days);
	}

	@GetMapping("/api/variant/low-stock")
	public List<ProductVariant> lowStock(@RequestParam(defaultValue = "5") int threshold) {
		return productService.getLowStockVariants(threshold);
	}

	// body carries productId
	@PostMapping("/api/variant")
	public ProductVariant add(@RequestBody ProductVariant variant) {
		return productService.addVariant(variant.getProductId(), variant);
	}

	@PutMapping("/api/variant")
	public ProductVariant update(@RequestBody ProductVariant variant) {
		return productService.updateVariant(variant);
	}

	@PutMapping("/api/variant/{id}/stock")
	public ProductVariant updateStock(@PathVariable int id, @RequestBody Map<String, Integer> body) {
		Integer quantity = body.get("quantity");
		if (quantity == null) {
			throw new com.amira.ms.exception.BusinessRuleException("quantity is required");
		}
		return productService.updateStock(id, quantity);
	}

	@PutMapping("/api/variant/{id}/deactivate")
	public ProductVariant deactivate(@PathVariable int id) {
		return productService.deactivateVariant(id);
	}

	@DeleteMapping("/api/variant/{id}")
	public void delete(@PathVariable int id) {
		productService.deleteVariant(id);
	}
}
