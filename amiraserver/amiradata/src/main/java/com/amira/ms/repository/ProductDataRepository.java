package com.amira.ms.repository;

import com.amira.ms.entity.ProductData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductDataRepository extends CrudRepository<ProductData, Integer> {
	// empty string means "no filter"
	@Query("select p from ProductData p where "
			+ "(:keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%')) "
			+ "  or lower(p.brand) like lower(concat('%', :keyword, '%')) "
			+ "  or lower(p.description) like lower(concat('%', :keyword, '%'))) "
			+ "and (:category = '' or p.category = :category) "
			+ "and (:brand = '' or lower(p.brand) = lower(:brand)) "
			+ "order by p.name")
	List<ProductData> search(@Param("keyword") String keyword, @Param("category") String category,
			@Param("brand") String brand);
}
