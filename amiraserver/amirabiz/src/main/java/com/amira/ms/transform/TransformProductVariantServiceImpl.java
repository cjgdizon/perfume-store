package com.amira.ms.transform;

import com.amira.ms.entity.ProductVariantData;
import com.amira.ms.model.ProductVariant;
import com.amira.ms.service.Rules;
import org.springframework.stereotype.Service;

@Service
public class TransformProductVariantServiceImpl implements TransformProductVariantService {
	@Override
	public ProductVariantData transform(ProductVariant variant) {
		ProductVariantData data = new ProductVariantData();
		data.setId(variant.getId());
		data.setProductId(variant.getProductId());
		data.setSize(variant.getSize());
		data.setSku(variant.getSku());
		data.setPrice(variant.getPrice());
		data.setCostPrice(variant.getCostPrice());
		data.setStockQuantity(variant.getStockQuantity());
		data.setReservedQuantity(variant.getReservedQuantity());
		data.setBatchNumber(variant.getBatchNumber());
		data.setManufactureDate(variant.getManufactureDate());
		data.setExpiryDate(variant.getExpiryDate());
		// only Discontinued is stored; Expired and LowStock are derived
		data.setStatus(Rules.DISCONTINUED.equals(variant.getStatus()) ? Rules.DISCONTINUED : Rules.ACTIVE);
		return data;
	}

	@Override
	public ProductVariant transform(ProductVariantData data) {
		ProductVariant variant = new ProductVariant();
		variant.setId(data.getId());
		variant.setProductId(data.getProductId());
		variant.setSize(data.getSize());
		variant.setSku(data.getSku());
		variant.setPrice(data.getPrice());
		variant.setCostPrice(data.getCostPrice());
		variant.setStockQuantity(data.getStockQuantity());
		variant.setReservedQuantity(data.getReservedQuantity());
		variant.setAvailableQuantity(Rules.available(data));
		variant.setBatchNumber(data.getBatchNumber());
		variant.setManufactureDate(data.getManufactureDate());
		variant.setExpiryDate(data.getExpiryDate());
		variant.setStatus(Rules.variantStatus(data));
		return variant;
	}
}
