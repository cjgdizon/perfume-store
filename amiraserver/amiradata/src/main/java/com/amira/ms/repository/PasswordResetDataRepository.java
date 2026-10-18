package com.amira.ms.repository;

import com.amira.ms.entity.PasswordResetData;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PasswordResetDataRepository extends CrudRepository<PasswordResetData, Integer> {
	Optional<PasswordResetData> findByTokenHash(String tokenHash);

	void deleteByCustomerId(int customerId);
}
