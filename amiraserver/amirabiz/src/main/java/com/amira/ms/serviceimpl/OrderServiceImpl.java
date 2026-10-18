package com.amira.ms.serviceimpl;

import com.amira.ms.entity.CustomerData;
import com.amira.ms.entity.OrderData;
import com.amira.ms.entity.OrderItemData;
import com.amira.ms.entity.PaymentData;
import com.amira.ms.entity.ProductData;
import com.amira.ms.entity.ProductVariantData;
import com.amira.ms.entity.StatusData;
import com.amira.ms.exception.BusinessRuleException;
import com.amira.ms.exception.NotFoundException;
import com.amira.ms.model.Order;
import com.amira.ms.model.OrderItem;
import com.amira.ms.repository.CustomerDataRepository;
import com.amira.ms.repository.OrderDataRepository;
import com.amira.ms.repository.OrderItemDataRepository;
import com.amira.ms.repository.PaymentDataRepository;
import com.amira.ms.repository.ProductDataRepository;
import com.amira.ms.repository.ProductVariantDataRepository;
import com.amira.ms.repository.StatusDataRepository;
import com.amira.ms.service.InventoryService;
import com.amira.ms.service.OrderService;
import com.amira.ms.service.Rules;
import com.amira.ms.transform.TransformOrderItemService;
import com.amira.ms.transform.TransformOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
	Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

	// allowed status moves; cancelling goes through cancelOrder
	private static final Map<String, Set<String>> TRANSITIONS = Map.of(
			Rules.PENDING, Set.of(Rules.PAID, Rules.CANCELLED),
			Rules.PAID, Set.of(Rules.SHIPPED, Rules.CANCELLED),
			Rules.SHIPPED, Set.of(Rules.DELIVERED),
			Rules.DELIVERED, Set.of(),
			Rules.CANCELLED, Set.of());

	@Autowired
	OrderDataRepository orderDataRepository;

	@Autowired
	OrderItemDataRepository orderItemDataRepository;

	@Autowired
	CustomerDataRepository customerDataRepository;

	@Autowired
	ProductDataRepository productDataRepository;

	@Autowired
	ProductVariantDataRepository variantDataRepository;

	@Autowired
	StatusDataRepository statusDataRepository;

	@Autowired
	PaymentDataRepository paymentDataRepository;

	@Autowired
	InventoryService inventoryService;

	@Autowired
	TransformOrderService transformOrderService;

	@Autowired
	TransformOrderItemService transformOrderItemService;

	@Override
	@Transactional(readOnly = true)
	public List<Order> getAll() {
		return toOrders(orderDataRepository.findAllByOrderByDateOrderedDesc());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Order> getOrderHistory(int customerId) {
		return toOrders(orderDataRepository.findAllByCustomerIdOrderByDateOrderedDesc(customerId));
	}

	@Override
	@Transactional(readOnly = true)
	public Order get(int id) {
		return toOrder(find(id));
	}

	@Override
	public Order create(Order order) {
		CustomerData customer = customerDataRepository.findById(order.getCustomerId())
				.orElseThrow(() -> new NotFoundException("Customer " + order.getCustomerId() + " not found"));
		if (order.getItems() == null || order.getItems().isEmpty()) {
			throw new BusinessRuleException("An order needs at least one item");
		}
		String address = order.getShippingAddress() != null && !order.getShippingAddress().isBlank()
				? order.getShippingAddress() : customer.getShippingAddress();
		if (address == null || address.isBlank()) {
			throw new BusinessRuleException("A shipping address is required");
		}

		// one line per variant, quantities summed
		Map<Integer, Integer> wanted = new LinkedHashMap<>();
		for (OrderItem item : order.getItems()) {
			if (item.getQuantity() <= 0) {
				throw new BusinessRuleException("Quantity must be at least 1");
			}
			wanted.merge(item.getVariantId(), item.getQuantity(), Integer::sum);
		}

		List<OrderItem> items = new ArrayList<>();
		for (Map.Entry<Integer, Integer> line : wanted.entrySet()) {
			ProductVariantData variant = variantDataRepository.findById(line.getKey())
					.orElseThrow(() -> new NotFoundException("Variant " + line.getKey() + " not found"));
			ProductData product = productDataRepository.findById(variant.getProductId())
					.orElseThrow(() -> new NotFoundException("Product " + variant.getProductId() + " not found"));
			if (!inventoryService.checkAvailability(variant.getId(), line.getValue())) {
				throw new BusinessRuleException("Not enough stock for " + product.getName() + " " + variant.getSize());
			}
			// any later failure rolls the whole transaction back, including these holds
			inventoryService.reserveStock(variant.getId(), line.getValue());
			OrderItem item = new OrderItem();
			item.setVariantId(variant.getId());
			item.setProductName(product.getName());
			item.setSize(variant.getSize());
			item.setQuantity(line.getValue());
			item.setPriceAtPurchase(variant.getPrice());
			item.setSubtotal(variant.getPrice().multiply(BigDecimal.valueOf(line.getValue())));
			items.add(item);
		}

		OrderData orderData = new OrderData();
		orderData.setDescription(order.getDescription());
		orderData.setCustomerId(customer.getId());
		orderData.setCustomerName(customer.getName());
		orderData.setShippingAddress(address);
		orderData.setDateOrdered(LocalDateTime.now());
		orderData.setTotalAmount(calculateTotal(items));
		orderData.setPaymentStatus(Rules.UNPAID);
		setStatus(orderData, Rules.PENDING);
		orderData = orderDataRepository.save(orderData);
		for (OrderItem item : items) {
			item.setOrderId(orderData.getId());
			orderItemDataRepository.save(transformOrderItemService.transform(item));
		}
		logger.info("created order {}", orderData);
		return toOrder(orderData);
	}

	@Override
	public BigDecimal calculateTotal(List<OrderItem> items) {
		BigDecimal total = BigDecimal.ZERO;
		for (OrderItem item : items) {
			total = total.add(item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())));
		}
		return total;
	}

	@Override
	public Order updateOrderStatus(int orderId, String statusName) {
		if (Rules.CANCELLED.equals(statusName)) {
			return cancelOrder(orderId);
		}
		OrderData orderData = find(orderId);
		if (!TRANSITIONS.getOrDefault(orderData.getStatusName(), Set.of()).contains(statusName)) {
			throw new BusinessRuleException(
					"An order cannot move from " + orderData.getStatusName() + " to " + statusName);
		}
		setStatus(orderData, statusName);
		return toOrder(orderDataRepository.save(orderData));
	}

	@Override
	public Order cancelOrder(int orderId) {
		OrderData orderData = find(orderId);
		if (!Rules.PENDING.equals(orderData.getStatusName()) && !Rules.PAID.equals(orderData.getStatusName())) {
			throw new BusinessRuleException("Only Pending or Paid orders can be cancelled");
		}
		boolean wasPaid = Rules.PAID.equals(orderData.getPaymentStatus());
		for (OrderItemData item : orderItemDataRepository.findAllByOrderId(orderId)) {
			if (wasPaid) {
				inventoryService.restoreStock(item.getVariantId(), item.getQuantity());
			} else {
				inventoryService.releaseReservation(item.getVariantId(), item.getQuantity());
			}
		}
		for (PaymentData payment : paymentDataRepository.findAllByOrderId(orderId)) {
			if (Rules.PAYMENT_COMPLETED.equals(payment.getStatus())) {
				payment.setStatus(Rules.REFUNDED);
				paymentDataRepository.save(payment);
			} else if (Rules.PAYMENT_PENDING.equals(payment.getStatus())) {
				payment.setStatus(Rules.PAYMENT_FAILED);
				paymentDataRepository.save(payment);
			}
		}
		if (wasPaid) {
			orderData.setPaymentStatus(Rules.REFUNDED);
		}
		setStatus(orderData, Rules.CANCELLED);
		return toOrder(orderDataRepository.save(orderData));
	}

	private OrderData find(int id) {
		return orderDataRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Order " + id + " not found"));
	}

	private void setStatus(OrderData orderData, String statusName) {
		StatusData status = statusDataRepository.findByName(statusName)
				.orElseThrow(() -> new NotFoundException("Status " + statusName + " is not configured"));
		orderData.setStatusId(status.getId());
		orderData.setStatusName(status.getName());
	}

	private Order toOrder(OrderData orderData) {
		Order order = transformOrderService.transform(orderData);
		order.setItems(orderItemDataRepository.findAllByOrderId(orderData.getId()).stream()
				.map(transformOrderItemService::transform).collect(Collectors.toList()));
		return order;
	}

	private List<Order> toOrders(List<OrderData> ordersData) {
		return ordersData.stream().map(this::toOrder).collect(Collectors.toList());
	}
}
