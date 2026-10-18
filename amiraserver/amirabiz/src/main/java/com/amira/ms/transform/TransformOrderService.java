package com.amira.ms.transform;

import com.amira.ms.entity.OrderData;
import com.amira.ms.model.Order;

public interface TransformOrderService {
	OrderData transform(Order order);

	// items are not mapped here; OrderService loads them
	Order transform(OrderData orderData);
}
