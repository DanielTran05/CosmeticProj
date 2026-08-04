package com.dtp.cosmemgt.sales.order.repository;

import com.dtp.cosmemgt.sales.order.entity.OrderShipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderShippingRepository extends JpaRepository<OrderShipping, String> {
}
