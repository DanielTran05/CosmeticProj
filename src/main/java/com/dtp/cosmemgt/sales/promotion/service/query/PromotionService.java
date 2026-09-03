package com.dtp.cosmemgt.sales.promotion.service.query;

import com.dtp.cosmemgt.sales.promotion.dto.response.PromotionResponse;
import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import com.dtp.cosmemgt.sales.promotion.mapper.PromotionMapper;
import com.dtp.cosmemgt.sales.promotion.repository.PromotionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class PromotionService {
    PromotionRepository promotionRepository;
    PromotionMapper promotionMapper;


    public List<PromotionResponse> getOrderVouchers() {

        List<Promotion> orderPromo = promotionRepository.findAvailableOrderVouchers(LocalDateTime.now());

        return orderPromo.stream().map(promotionMapper::toPromotionResponse).toList();
    }
}
