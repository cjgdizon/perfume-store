package com.amira.ms.service;

import com.amira.ms.model.Order;
import com.amira.ms.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
	List<Order> getAll();

	List<Order> getOrderHistory(int customerId);

	Order get(int id);

	// customerId + items (variantId, quantity); checks and reserves stock
	Order create(Order order);

	BigDecimal calculateTotal(List<OrderItem> items);

	Order updateOrderStatus(int orderId, String statusName);

	// Pending or Paid orders only; releases or restores stock and refunds a completed payment
	Order cancelOrder(int orderId);
}
