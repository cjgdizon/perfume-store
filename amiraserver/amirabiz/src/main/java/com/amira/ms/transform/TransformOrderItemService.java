package com.amira.ms.transform;

import com.amira.ms.entity.OrderItemData;
import com.amira.ms.model.OrderItem;

public interface TransformOrderItemService {
	OrderItemData transform(OrderItem orderItem);

	OrderItem transform(OrderItemData orderItemData);
}
