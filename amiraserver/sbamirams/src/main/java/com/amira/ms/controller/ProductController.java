package com.amira.ms.controller;

import com.amira.ms.config.AuthContext;
import com.amira.ms.model.Product;
import com.amira.ms.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class ProductController {
	Logger logger = LoggerFactory.getLogger(ProductController.class);

	@Autowired
	private ProductService productService;

	@GetMapping("/api/product")
	public List<Product> list(@RequestParam(defaultValue = "") String keyword,
			@RequestParam(defaultValue = "") String category, @RequestParam(defaultValue = "") String brand,
			HttpServletRequest request) {
		List<Product> products = productService.searchProducts(keyword, category, brand);
		products.forEach(p -> scrub(p, request));
		return products;
	}

	@GetMapping("/api/product/{id}")
	public Product get(@PathVariable int id, HttpServletRequest request) {
		return scrub(productService.get(id), request);
	}

	@PostMapping("/api/product")
	public Product add(@RequestBody Product product) {
		logger.info("Input >> {}", product);
		return productService.create(product);
	}

	@PutMapping("/api/product")
	public Product update(@RequestBody Product product) {
		return productService.update(product);
	}

	@DeleteMapping("/api/product/{id}")
	public void delete(@PathVariable int id) {
		productService.delete(id);
	}

	// cost price is the owner's margin information: shoppers never receive it
	static Product scrub(Product product, HttpServletRequest request) {
		if (!AuthContext.isOwner(request)) {
			product.getVariants().forEach(v -> v.setCostPrice(null));
		}
		return product;
	}
}
