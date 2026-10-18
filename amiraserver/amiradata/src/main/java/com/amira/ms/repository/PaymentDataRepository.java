package com.amira.ms.repository;

import com.amira.ms.entity.PaymentData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PaymentDataRepository extends CrudRepository<PaymentData, Integer> {
	List<PaymentData> findAllByOrderId(int orderId);
}
