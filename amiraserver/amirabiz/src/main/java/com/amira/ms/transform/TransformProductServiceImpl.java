package com.amira.ms.transform;

import com.amira.ms.entity.ProductData;
import com.amira.ms.model.Product;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TransformProductServiceImpl implements TransformProductService {
	@Override
	public ProductData transform(Product product) {
		ProductData productData = new ProductData();
		productData.setId(product.getId());
		productData.setName(product.getName());
		productData.setBrand(product.getBrand());
		productData.setDescription(product.getDescription());
		productData.setCategory(product.getCategory());
		productData.setImageUrl(product.getImageUrl());
		productData.setDateAdded(product.getDateAdded() != null ? product.getDateAdded() : LocalDate.now());
		return productData;
	}

	@Override
	public Product transform(ProductData productData) {
		Product product = new Product();
		product.setId(productData.getId());
		product.setName(productData.getName());
		product.setBrand(productData.getBrand());
		product.setDescription(productData.getDescription());
		product.setCategory(productData.getCategory());
		product.setImageUrl(productData.getImageUrl());
		product.setDateAdded(productData.getDateAdded());
		return product;
	}
}
