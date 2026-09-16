package com.dtp.cosmemgt.catalog.service.specification;

import com.dtp.cosmemgt.catalog.entity.Category;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.sales.promotion.repository.PromotionRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ProductSpecification {

    private final PromotionRepository promotionRepository;

    public static Specification<Product> filterProductForAdmin(Map<String, String> queryParams){
        return (root, query, criteriaBuilder) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        //name like %nameValue%
                        if(queryParams.containsKey("kw") && !queryParams.get("kw").isEmpty()){
                            String nameSearch = "%"+queryParams.get("kw").toLowerCase()+"%";
                            predicates.add(criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get("name")),
                                    nameSearch));
                        }

                        if(queryParams.containsKey("minPrice") && queryParams.get("minPrice") != null){
                            try{
                                Double minPrice = Double.parseDouble(queryParams.get("minPrice"));
                                predicates.add(criteriaBuilder.ge(
                                        criteriaBuilder.coalesce(root.get("minDiscountedPrice"), root.get("minPrice")),
                                        minPrice
                                ));
                            }catch (NumberFormatException e){
                                log.error("Invalid minPrice value: {}", queryParams.get("minPrice"), e);
                            }
                        }

                        if(queryParams.containsKey("maxPrice") && queryParams.get("maxPrice") != null){
                            try{
                                Double maxPrice = Double.parseDouble(queryParams.get("maxPrice"));
                                predicates.add(criteriaBuilder.le(
                                        criteriaBuilder.coalesce(root.get("minDiscountedPrice"), root.get("minPrice")),
                                        maxPrice
                                ));
                            }catch (NumberFormatException e){
                                log.error("Invalid maxPrice value: {}", queryParams.get("maxPrice"), e);
                            }
                        }

                        if(queryParams.containsKey("cateId") && queryParams.get("cateId") != null){
                            try{
                                int cateId = Integer.parseInt(queryParams.get("cateId"));
                                predicates.add(criteriaBuilder.equal(root.get("category").get("id"),
                                        cateId));
                            }catch(NumberFormatException e){
                                log.error("Invalid cateId value: {}", queryParams.get("cateId"), e);
                            }
                        }

                        if((queryParams.containsKey("isDiscounted") && Boolean.parseBoolean((queryParams.get("isDiscounted"))))){
                            predicates.add(criteriaBuilder.lessThan(root.get("minDiscountedPrice"), root.get("minPrice")));
                        }

                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

    }

    public static Specification<Product> filterProductForCustomer(Map<String, String> params) {
        Specification<Product> customerActiveSpec = (root, query, cb) -> {
            Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);

            return cb.and(
                    cb.isNull(root.get("deletedAt")),
                    cb.isNotNull(root.get("category")),
                    cb.isNull(categoryJoin.get("deletedAt"))
            );
        };

        return filterProductForAdmin(params).and(customerActiveSpec);
    }
}
