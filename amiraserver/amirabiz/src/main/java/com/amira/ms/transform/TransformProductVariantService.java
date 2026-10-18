package com.amira.ms.transform;

import com.amira.ms.entity.ProductVariantData;
import com.amira.ms.model.ProductVariant;

public interface TransformProductVariantService {
	ProductVariantData transform(ProductVariant variant);

	ProductVariant transform(ProductVariantData variantData);
}
