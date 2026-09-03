package com.dtp.cosmemgt.sales.promotion.service.query;

import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import com.dtp.cosmemgt.sales.promotion.enums.DiscountType;
import com.dtp.cosmemgt.sales.promotion.enums.ScopeType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PromotionSpecification {

    public static Specification<Promotion> filterPromotionForAdmin(java.util.Map<String, String> queryParams) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(queryParams.containsKey("kw") && !queryParams.get("kw").isEmpty()){
                String nameSearch = "%"+queryParams.get("kw").toLowerCase()+"%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        nameSearch));
            }

            if(queryParams.containsKey("code") && !queryParams.get("code").isEmpty()){
                String codeSearch = "%"+queryParams.get("code").toLowerCase()+"%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")),
                        codeSearch));
            }

            String discountType = queryParams.get("discountType");
            if (StringUtils.hasText(discountType)) {
                try {
                    DiscountType enumVal = DiscountType.valueOf(discountType.trim().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("discountType"), enumVal));
                } catch (IllegalArgumentException ignored) {

                }
            }

            String scopeType = queryParams.get("scopeType");
            if(StringUtils.hasText(scopeType)){
                try{
                    ScopeType enumVal = ScopeType.valueOf(scopeType.trim().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("scopeType"), enumVal));
                }catch (IllegalArgumentException ignored){

                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Promotion> filterPromotionForCustomer(Map<String, String> params) {
        Specification<Promotion> customerActiveSpec = (root, query, cb) -> cb.and(
                cb.isNull(root.get("deletedAt")),
                cb.isNull(root.get("category").get("deletedAt"))
        );

        return filterPromotionForAdmin(params).and(customerActiveSpec);
    }
}
