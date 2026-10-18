package com.amira.ms.serviceimpl;

import com.amira.ms.entity.OrderData;
import com.amira.ms.entity.OrderItemData;
import com.amira.ms.entity.PaymentData;
import com.amira.ms.exception.BusinessRuleException;
import com.amira.ms.exception.NotFoundException;
import com.amira.ms.model.Payment;
import com.amira.ms.repository.OrderDataRepository;
import com.amira.ms.repository.OrderItemDataRepository;
import com.amira.ms.repository.PaymentDataRepository;
import com.amira.ms.service.InventoryService;
import com.amira.ms.service.OrderService;
import com.amira.ms.service.PaymentService;
import com.amira.ms.service.Rules;
import com.amira.ms.transform.TransformPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
	Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

	private static final Set<String> METHODS = Set.of("GCash", "Card", "Bank Transfer", "COD");
	// ponytail: GCash and Card use a simulated gateway that approves immediately. Bank Transfer
	// and COD stay Pending until the owner confirms. Swap in a real gateway client here later.
	private static final Set<String> INSTANT_METHODS = Set.of("GCash", "Card");

	@Autowired
	PaymentDataRepository paymentDataRepository;

	@Autowired
	OrderDataRepository orderDataRepository;

	@Autowired
	OrderItemDataRepository orderItemDataRepository;

	@Autowired
	OrderService orderService;

	@Autowired
	InventoryService inventoryService;

	@Autowired
	TransformPaymentService transformPaymentService;

	@Override
	@Transactional(readOnly = true)
	public Payment get(int id) {
		return transformPaymentService.transform(find(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Payment> listByOrderId(int orderId) {
		return paymentDataRepository.findAllByOrderId(orderId).stream()
				.map(transformPaymentService::transform).collect(Collectors.toList());
	}

	@Override
	public Payment processPayment(int orderId, String method) {
		if (method == null || !METHODS.contains(method)) {
			throw new BusinessRuleException("Payment method must be one of GCash, Card, Bank Transfer, COD");
		}
		OrderData order = orderDataRepository.findById(orderId)
				.orElseThrow(() -> new NotFoundException("Order " + orderId + " not found"));
		if (!Rules.PENDING.equals(order.getStatusName()) || !Rules.UNPAID.equals(order.getPaymentStatus())) {
			throw new BusinessRuleException("Only Pending, unpaid orders can be paid");
		}
		for (PaymentData existing : paymentDataRepository.findAllByOrderId(orderId)) {
			if (Rules.PAYMENT_PENDING.equals(existing.getStatus())) {
				throw new BusinessRuleException("This order already has a payment awaiting confirmation");
			}
		}
		PaymentData payment = new PaymentData();
		payment.setOrderId(orderId);
		payment.setMethod(method);
		payment.setAmount(order.getTotalAmount());
		payment.setPaymentDate(LocalDateTime.now());
		payment.setStatus(Rules.PAYMENT_PENDING);
		payment = paymentDataRepository.save(payment);
		if (INSTANT_METHODS.contains(method)) {
			return confirmPayment(payment.getId());
		}
		return transformPaymentService.transform(payment);
	}

	@Override
	public Payment confirmPayment(int paymentId) {
		PaymentData payment = find(paymentId);
		if (!Rules.PAYMENT_PENDING.equals(payment.getStatus())) {
			throw new BusinessRuleException("Only a Pending payment can be confirmed");
		}
		OrderData order = orderDataRepository.findById(payment.getOrderId())
				.orElseThrow(() -> new NotFoundException("Order " + payment.getOrderId() + " not found"));
		if (!Rules.PENDING.equals(order.getStatusName())) {
			throw new BusinessRuleException("The order is " + order.getStatusName() + " and can no longer be paid");
		}
		for (OrderItemData item : orderItemDataRepository.findAllByOrderId(order.getId())) {
			inventoryService.deductStock(item.getVariantId(), item.getQuantity());
		}
		payment.setStatus(Rules.PAYMENT_COMPLETED);
		payment.setPaymentDate(LocalDateTime.now());
		paymentDataRepository.save(payment);
		order.setPaymentStatus(Rules.PAID);
		orderDataRepository.save(order);
		orderService.updateOrderStatus(order.getId(), Rules.PAID);
		logger.info("confirmed payment {}", payment);
		return transformPaymentService.transform(payment);
	}

	@Override
	public Payment refundPayment(int paymentId) {
		PaymentData payment = find(paymentId);
		if (!Rules.PAYMENT_COMPLETED.equals(payment.getStatus())) {
			throw new BusinessRuleException("Only a Completed payment can be refunded");
		}
		OrderData order = orderDataRepository.findById(payment.getOrderId())
				.orElseThrow(() -> new NotFoundException("Order " + payment.getOrderId() + " not found"));
		// before shipping, cancelling the order refunds and restores stock in one step
		if (!Rules.DELIVERED.equals(order.getStatusName())) {
			throw new BusinessRuleException("Cancel the order to refund it before it ships");
		}
		// returned goods are not put back on sale automatically: the owner re-counts them with updateStock
		payment.setStatus(Rules.REFUNDED);
		paymentDataRepository.save(payment);
		order.setPaymentStatus(Rules.REFUNDED);
		orderDataRepository.save(order);
		return transformPaymentService.transform(payment);
	}

	private PaymentData find(int id) {
		return paymentDataRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Payment " + id + " not found"));
	}
}
