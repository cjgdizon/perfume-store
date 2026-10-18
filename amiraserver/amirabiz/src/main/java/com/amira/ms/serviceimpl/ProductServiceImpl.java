package com.amira.ms.serviceimpl;

import com.amira.ms.entity.ProductData;
import com.amira.ms.entity.ProductVariantData;
import com.amira.ms.exception.BusinessRuleException;
import com.amira.ms.exception.NotFoundException;
import com.amira.ms.model.Product;
import com.amira.ms.model.ProductVariant;
import com.amira.ms.repository.OrderItemDataRepository;
import com.amira.ms.repository.ProductDataRepository;
import com.amira.ms.repository.ProductVariantDataRepository;
import com.amira.ms.service.ProductService;
import com.amira.ms.service.Rules;
import com.amira.ms.transform.TransformProductService;
import com.amira.ms.transform.TransformProductVariantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
	Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

	@Autowired
	ProductDataRepository productDataRepository;

	@Autowired
	ProductVariantDataRepository variantDataRepository;

	@Autowired
	OrderItemDataRepository orderItemDataRepository;

	@Autowired
	TransformProductService transformProductService;

	@Autowired
	TransformProductVariantService transformVariantService;

	@Override
	@Transactional(readOnly = true)
	public List<Product> getAll() {
		return searchProducts("", "", "");
	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> searchProducts(String keyword, String category, String brand) {
		List<ProductData> productsData = productDataRepository.search(blank(keyword), blank(category), blank(brand));
		List<Integer> ids = productsData.stream().map(ProductData::getId).collect(Collectors.toList());
		Map<Integer, List<ProductVariant>> variantsByProduct = new HashMap<>();
		if (!ids.isEmpty()) {
			for (ProductVariantData v : variantDataRepository.findAllByProductIdIn(ids)) {
				variantsByProduct.computeIfAbsent(v.getProductId(), k -> new ArrayList<>())
						.add(transformVariantService.transform(v));
			}
		}
		List<Product> products = new ArrayList<>();
		for (ProductData productData : productsData) {
			Product product = transformProductService.transform(productData);
			product.setVariants(variantsByProduct.getOrDefault(productData.getId(), new ArrayList<>()));
			products.add(product);
		}
		return products;
	}

	@Override
	@Transactional(readOnly = true)
	public Product get(int id) {
		ProductData productData = productDataRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
		Product product = transformProductService.transform(productData);
		product.setVariants(variantDataRepository.findAllByProductId(id).stream()
				.map(transformVariantService::transform).collect(Collectors.toList()));
		return product;
	}

	@Override
	public Product create(Product product) {
		requireText(product.getName(), "Product name is required");
		requireText(product.getCategory(), "Category is required (Men, Women or Unisex)");
		ProductData productData = transformProductService.transform(product);
		productData.setId(0);
		productData = productDataRepository.save(productData);
		logger.info("created product {}", productData);
		if (product.getVariants() != null) {
			for (ProductVariant variant : product.getVariants()) {
				addVariant(productData.getId(), variant);
			}
		}
		return get(productData.getId());
	}

	@Override
	public Product update(Product product) {
		ProductData existing = productDataRepository.findById(product.getId())
				.orElseThrow(() -> new NotFoundException("Product " + product.getId() + " not found"));
		requireText(product.getName(), "Product name is required");
		ProductData productData = transformProductService.transform(product);
		productData.setDateAdded(existing.getDateAdded());
		productDataRepository.save(productData);
		return get(product.getId());
	}

	@Override
	public void delete(int id) {
		ProductData productData = productDataRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
		List<ProductVariantData> variants = variantDataRepository.findAllByProductId(id);
		for (ProductVariantData v : variants) {
			if (orderItemDataRepository.existsByVariantId(v.getId())) {
				throw new BusinessRuleException("This product has order history. Discontinue its variants instead.");
			}
		}
		variantDataRepository.deleteAll(variants);
		productDataRepository.delete(productData);
	}

	@Override
	@Transactional(readOnly = true)
	public ProductVariant getVariant(int variantId) {
		ProductVariant variant = transformVariantService.transform(findVariant(variantId));
		variant.setProductName(productName(variant.getProductId()));
		return variant;
	}

	@Override
	public ProductVariant addVariant(int productId, ProductVariant variant) {
		if (!productDataRepository.existsById(productId)) {
			throw new NotFoundException("Product " + productId + " not found");
		}
		validateVariant(variant);
		ProductVariantData data = transformVariantService.transform(variant);
		data.setId(0);
		data.setProductId(productId);
		data.setReservedQuantity(0);
		data.setStatus(Rules.ACTIVE);
		if (data.getSku() == null || data.getSku().isBlank()) {
			data.setSku("AM-" + productId + "-" + data.getSize().replaceAll("\\s+", "").toUpperCase());
		}
		return transformVariantService.transform(variantDataRepository.save(data));
	}

	@Override
	public ProductVariant updateVariant(ProductVariant variant) {
		ProductVariantData existing = findVariant(variant.getId());
		validateVariant(variant);
		ProductVariantData data = transformVariantService.transform(variant);
		data.setProductId(existing.getProductId());
		// stock changes go through updateStock / the order flow, never through a plain edit
		data.setStockQuantity(existing.getStockQuantity());
		data.setReservedQuantity(existing.getReservedQuantity());
		data.setStatus(existing.getStatus());
		return transformVariantService.transform(variantDataRepository.save(data));
	}

	@Override
	public ProductVariant updateStock(int variantId, int quantity) {
		ProductVariantData data = variantDataRepository.findByIdForUpdate(variantId)
				.orElseThrow(() -> new NotFoundException("Variant " + variantId + " not found"));
		if (quantity < data.getReservedQuantity()) {
			throw new BusinessRuleException(
					"Stock cannot be lower than the " + data.getReservedQuantity() + " units reserved for open orders");
		}
		data.setStockQuantity(quantity);
		return transformVariantService.transform(variantDataRepository.save(data));
	}

	@Override
	public ProductVariant deactivateVariant(int variantId) {
		ProductVariantData data = findVariant(variantId);
		data.setStatus(Rules.DISCONTINUED);
		return transformVariantService.transform(variantDataRepository.save(data));
	}

	@Override
	public void deleteVariant(int variantId) {
		ProductVariantData data = findVariant(variantId);
		if (orderItemDataRepository.existsByVariantId(variantId)) {
			throw new BusinessRuleException("This variant has order history. Deactivate it instead.");
		}
		variantDataRepository.delete(data);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductVariant> getExpiringVariants(int daysThreshold) {
		return withProductNames(variantDataRepository.findExpiringBy(LocalDate.now().plusDays(daysThreshold)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductVariant> getLowStockVariants(int threshold) {
		return withProductNames(variantDataRepository.findLowStock(threshold));
	}

	private List<ProductVariant> withProductNames(List<ProductVariantData> variants) {
		Map<Integer, String> names = new HashMap<>();
		List<ProductVariant> result = new ArrayList<>();
		for (ProductVariantData v : variants) {
			ProductVariant variant = transformVariantService.transform(v);
			variant.setProductName(names.computeIfAbsent(v.getProductId(), this::productName));
			result.add(variant);
		}
		return result;
	}

	private String productName(int productId) {
		return productDataRepository.findById(productId).map(ProductData::getName).orElse("");
	}

	private ProductVariantData findVariant(int variantId) {
		return variantDataRepository.findById(variantId)
				.orElseThrow(() -> new NotFoundException("Variant " + variantId + " not found"));
	}

	private void validateVariant(ProductVariant v) {
		requireText(v.getSize(), "Size is required (e.g. 50ml)");
		if (v.getPrice() == null || v.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessRuleException("Price must be greater than zero");
		}
		if (v.getCostPrice() != null && v.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
			throw new BusinessRuleException("Cost price cannot be negative");
		}
		if (v.getStockQuantity() < 0) {
			throw new BusinessRuleException("Stock cannot be negative");
		}
		if (v.getManufactureDate() != null && v.getExpiryDate() != null
				&& v.getExpiryDate().isBefore(v.getManufactureDate())) {
			throw new BusinessRuleException("Expiry date cannot be before the manufacture date");
		}
	}

	private static void requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new BusinessRuleException(message);
		}
	}

	private static String blank(String value) {
		return value == null ? "" : value.trim();
	}
}
