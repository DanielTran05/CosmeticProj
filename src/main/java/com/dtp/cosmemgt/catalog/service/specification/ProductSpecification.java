package com.dtp.cosmemgt.catalog.service.specification;

import com.dtp.cosmemgt.catalog.entity.Product;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProductSpecification {

    public static Specification<Product> filterProduct(Map<String, String> queryParams){
        return (root, query, criteriaBuilder) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        //name like %nameValue%
                        if(queryParams.containsKey("name") && !queryParams.get("name").isEmpty()){
                            String nameSearch = "%"+queryParams.get("name").toLowerCase()+"%";
                            predicates.add(criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get("name")),
                                    nameSearch));
                        }

                        if(queryParams.containsKey("minPrice") && queryParams.get("minPrice") != null){
                            try{
                                Double minPrice = Double.parseDouble(queryParams.get("minPrice"));
                                predicates.add(criteriaBuilder.ge(root.get("basePrice"), minPrice));
                            }catch (NumberFormatException e){
                                log.error("Invalid minPrice value: {}", queryParams.get("minPrice"), e);
                            }
                        }

                        if(queryParams.containsKey("maxPrice") && queryParams.get("maxPrice") != null){
                            try{
                                Double maxPrice = Double.parseDouble(queryParams.get("maxPrice"));
                                predicates.add(criteriaBuilder.le(root.get("basePrice"), maxPrice));
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
                            };
                        }

                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

    }
}
