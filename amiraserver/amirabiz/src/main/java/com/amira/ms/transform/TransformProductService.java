package com.amira.ms.transform;

import com.amira.ms.entity.ProductData;
import com.amira.ms.model.Product;

public interface TransformProductService {
	ProductData transform(Product product);

	Product transform(ProductData productData);
}
