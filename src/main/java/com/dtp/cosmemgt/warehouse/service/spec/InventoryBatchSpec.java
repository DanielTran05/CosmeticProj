package com.dtp.cosmemgt.warehouse.service.spec;

import com.dtp.cosmemgt.warehouse.entity.InventoryBatch;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryBatchSpec {
    public static Specification<InventoryBatch> filterBatch(Map<String, String> queryParams){
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(queryParams.containsKey("productVariantId") && !queryParams.get("productVariantId").isBlank()){
                try{
                    predicates.add(criteriaBuilder.equal(
                            root.get("productVariant").get("id")
                            ,queryParams.get("productVariantId")));
                }catch (IllegalArgumentException e){
                    //nothing
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

    }
}
