package com.amira.ms.repository;

import com.amira.ms.entity.OrderItemData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemDataRepository extends CrudRepository<OrderItemData, Integer> {
	List<OrderItemData> findAllByOrderId(int orderId);

	boolean existsByVariantId(int variantId);

	// only paid, non-cancelled orders count as sales
	@Query("select i.variantId, i.productName, i.size, sum(i.quantity), sum(i.subtotal) "
			+ "from OrderItemData i, OrderData o where o.id = i.orderId "
			+ "and o.statusName <> 'Cancelled' and o.paymentStatus = 'Paid' "
			+ "and o.dateOrdered between :from and :to "
			+ "group by i.variantId, i.productName, i.size order by sum(i.quantity) desc")
	List<Object[]> bestSellers(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("select p.category, sum(i.subtotal) "
			+ "from OrderItemData i, OrderData o, ProductVariantData v, ProductData p "
			+ "where o.id = i.orderId and v.id = i.variantId and p.id = v.productId "
			+ "and o.statusName <> 'Cancelled' and o.paymentStatus = 'Paid' "
			+ "group by p.category order by sum(i.subtotal) desc")
	List<Object[]> revenueByCategory();

	@Query("select distinct i.variantId from OrderItemData i, OrderData o where o.id = i.orderId "
			+ "and o.statusName <> 'Cancelled' and o.dateOrdered >= :since")
	List<Integer> variantIdsSoldSince(@Param("since") LocalDateTime since);
}
