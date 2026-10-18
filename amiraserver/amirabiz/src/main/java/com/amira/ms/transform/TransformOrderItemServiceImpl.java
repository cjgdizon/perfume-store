package com.amira.ms.transform;

import com.amira.ms.entity.OrderItemData;
import com.amira.ms.model.OrderItem;
import org.springframework.stereotype.Service;

@Service
public class TransformOrderItemServiceImpl implements TransformOrderItemService {
	@Override
	public OrderItemData transform(OrderItem item) {
		OrderItemData data = new OrderItemData();
		data.setId(item.getId());
		data.setOrderId(item.getOrderId());
		data.setVariantId(item.getVariantId());
		data.setProductName(item.getProductName());
		data.setSize(item.getSize());
		data.setQuantity(item.getQuantity());
		data.setPriceAtPurchase(item.getPriceAtPurchase());
		data.setSubtotal(item.getSubtotal());
		return data;
	}

	@Override
	public OrderItem transform(OrderItemData data) {
		OrderItem item = new OrderItem();
		item.setId(data.getId());
		item.setOrderId(data.getOrderId());
		item.setVariantId(data.getVariantId());
		item.setProductName(data.getProductName());
		item.setSize(data.getSize());
		item.setQuantity(data.getQuantity());
		item.setPriceAtPurchase(data.getPriceAtPurchase());
		item.setSubtotal(data.getSubtotal());
		return item;
	}
}
