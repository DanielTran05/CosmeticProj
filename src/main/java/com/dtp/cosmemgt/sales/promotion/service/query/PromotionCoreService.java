package com.dtp.cosmemgt.sales.promotion.service.query;

import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import com.dtp.cosmemgt.sales.promotion.repository.PromotionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class PromotionCoreService {
    PromotionRepository promotionRepository;

    public Page<Promotion> getAll(Map<String, String> queryParams, boolean isAdmin) {
        Specification<Promotion> spec;
        if(isAdmin) {
            spec = PromotionSpecification.filterPromotionForAdmin(queryParams);
        } else {
            spec = PromotionSpecification.filterPromotionForCustomer(queryParams);
        }

        int page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 0;
        int size = queryParams.containsKey("size") ? Integer.parseInt(queryParams.get("size")) : 10;

        Sort sort = Sort.by("createdAt").descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return promotionRepository.findAll(spec, pageable);
    }


}
