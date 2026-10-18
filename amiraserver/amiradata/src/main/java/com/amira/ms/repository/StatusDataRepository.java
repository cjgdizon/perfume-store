package com.amira.ms.repository;

import com.amira.ms.entity.StatusData;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StatusDataRepository extends CrudRepository<StatusData, Integer> {
	Optional<StatusData> findByName(String name);
}
