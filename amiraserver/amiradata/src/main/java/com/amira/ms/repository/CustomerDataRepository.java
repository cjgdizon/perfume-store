package com.amira.ms.repository;

import com.amira.ms.entity.CustomerData;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CustomerDataRepository extends CrudRepository<CustomerData, Integer> {
	Optional<CustomerData> findByEmailIgnoreCase(String email);

	boolean existsByRole(String role);
}
