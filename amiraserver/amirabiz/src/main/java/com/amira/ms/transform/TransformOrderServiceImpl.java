package com.amira.ms.transform;

import com.amira.ms.entity.OrderData;
import com.amira.ms.model.Order;
import org.springframework.stereotype.Service;

@Service
public class TransformOrderServiceImpl implements TransformOrderService {
	@Override
	public OrderData transform(Order order) {
		OrderData data = new OrderData();
		data.setId(order.getId());
		data.setDescription(order.getDescription());
		data.setCustomerId(order.getCustomerId());
		data.setCustomerName(order.getCustomerName());
		data.setShippingAddress(order.getShippingAddress());
		data.setDateOrdered(order.getDateOrdered());
		data.setTotalAmount(order.getTotalAmount());
		data.setStatusId(order.getStatusId());
		data.setStatusName(order.getStatusName());
		data.setPaymentStatus(order.getPaymentStatus());
		return data;
	}

	@Override
	public Order transform(OrderData data) {
		Order order = new Order();
		order.setId(data.getId());
		order.setDescription(data.getDescription());
		order.setCustomerId(data.getCustomerId());
		order.setCustomerName(data.getCustomerName());
		order.setShippingAddress(data.getShippingAddress());
		order.setDateOrdered(data.getDateOrdered());
		order.setTotalAmount(data.getTotalAmount());
		order.setStatusId(data.getStatusId());
		order.setStatusName(data.getStatusName());
		order.setPaymentStatus(data.getPaymentStatus());
		return order;
	}
}
