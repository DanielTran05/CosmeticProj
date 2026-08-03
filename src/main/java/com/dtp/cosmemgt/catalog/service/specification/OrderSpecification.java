package com.dtp.cosmemgt.catalog.service.specification;

import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.sales.entity.Order;
import com.dtp.cosmemgt.sales.enums.OrderStatusEnum;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class OrderSpecification {

    public static Specification<Order> filterOrder(Map<String, String> queryParams){
        return (root, query, criteriaBuilder) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        if(queryParams.containsKey("status") && !queryParams.get("status").isEmpty()){
                            String statusString = queryParams.get("status").replace("\"", "").trim();

                            if(!statusString.isEmpty()) {
                                    try {
                                        OrderStatusEnum orderStatusEnum = OrderStatusEnum.valueOf(queryParams.get("status").toUpperCase());
                                        predicates.add(criteriaBuilder.equal(root.get("orderStatus"), orderStatusEnum));
                                    } catch (IllegalArgumentException e) {
                                        log.info("Invalid order status: {}", statusString);
                                }
                            }
                        }

                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

    }
}
