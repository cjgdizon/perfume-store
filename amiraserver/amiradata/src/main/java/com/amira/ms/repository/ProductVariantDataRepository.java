package com.amira.ms.repository;

import com.amira.ms.entity.ProductVariantData;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductVariantDataRepository extends CrudRepository<ProductVariantData, Integer> {
	List<ProductVariantData> findAllByProductId(int productId);

	List<ProductVariantData> findAllByProductIdIn(Collection<Integer> productIds);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select v from ProductVariantData v where v.id = :id")
	Optional<ProductVariantData> findByIdForUpdate(@Param("id") int id);

	// already expired variants are included: they are the most at-risk stock
	@Query("select v from ProductVariantData v where v.expiryDate is not null and v.expiryDate <= :limit "
			+ "and v.status <> 'Discontinued' order by v.expiryDate")
	List<ProductVariantData> findExpiringBy(@Param("limit") LocalDate limit);

	@Query("select v from ProductVariantData v where (v.stockQuantity - v.reservedQuantity) <= :threshold "
			+ "and v.status <> 'Discontinued' order by (v.stockQuantity - v.reservedQuantity)")
	List<ProductVariantData> findLowStock(@Param("threshold") int threshold);
}
