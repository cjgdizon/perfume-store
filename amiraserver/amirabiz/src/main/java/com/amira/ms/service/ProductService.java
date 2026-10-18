package com.amira.ms.service;

import com.amira.ms.model.Product;
import com.amira.ms.model.ProductVariant;

import java.util.List;

/** Catalog: perfumes (Product) and their bottle sizes (ProductVariant). */
public interface ProductService {
	List<Product> getAll();

	// empty string means "no filter"
	List<Product> searchProducts(String keyword, String category, String brand);

	Product get(int id);

	Product create(Product product);

	Product update(Product product);

	void delete(int id);


	ProductVariant getVariant(int variantId);

	ProductVariant addVariant(int productId, ProductVariant variant);

	ProductVariant updateVariant(ProductVariant variant);

	// manual reconciliation: sets the on-hand count to an absolute number
	ProductVariant updateStock(int variantId, int quantity);

	ProductVariant deactivateVariant(int variantId);

	void deleteVariant(int variantId);

	List<ProductVariant> getExpiringVariants(int daysThreshold);

	List<ProductVariant> getLowStockVariants(int threshold);
}
