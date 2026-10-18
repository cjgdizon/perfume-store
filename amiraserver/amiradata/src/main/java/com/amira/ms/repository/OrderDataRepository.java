package com.amira.ms.repository;

import com.amira.ms.entity.OrderData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderDataRepository extends CrudRepository<OrderData, Integer> {
	List<OrderData> findAllByOrderByDateOrderedDesc();

	List<OrderData> findAllByCustomerIdOrderByDateOrderedDesc(int customerId);
}
