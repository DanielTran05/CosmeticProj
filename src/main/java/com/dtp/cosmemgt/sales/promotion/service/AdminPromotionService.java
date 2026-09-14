package com.dtp.cosmemgt.sales.promotion.service;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.catalog.service.command.ProductSyncService;
import com.dtp.cosmemgt.core.commonService.CurrentUserService;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.promotion.dto.request.PromotionCreationRequest;
import com.dtp.cosmemgt.sales.promotion.dto.request.PromotionUpdateRequest;
import com.dtp.cosmemgt.sales.promotion.dto.request.TargetItemRequest;
import com.dtp.cosmemgt.sales.promotion.dto.response.AdminPromotionDetailResponse;
import com.dtp.cosmemgt.sales.promotion.dto.response.AdminPromotionResponse;
import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import com.dtp.cosmemgt.sales.promotion.entity.PromotionTargetItem;
import com.dtp.cosmemgt.sales.promotion.enums.ScopeType;
import com.dtp.cosmemgt.sales.promotion.mapper.PromotionMapper;
import com.dtp.cosmemgt.sales.promotion.repository.PromotionRepository;
import com.dtp.cosmemgt.sales.promotion.repository.PromotionTargetItemRepository;
import com.dtp.cosmemgt.sales.promotion.service.query.PromotionCoreService;
import com.dtp.cosmemgt.sales.promotion.utils.TargetItemsUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class AdminPromotionService {
    PromotionMapper promotionMapper;
    PromotionRepository promotionRepository;
    PromotionTargetItemRepository promotionTargetItemRepository;

    PromotionCoreService promotionCoreService;
    CurrentUserService currentUserService;

    TargetItemsUtils syncByTargetItems;

    public AdminPromotionResponse create(PromotionCreationRequest request) {
        if(promotionRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.PROMOTION_EXISTED);
        }

        if((request.getScopeType() == ScopeType.PRODUCT || request.getScopeType() == ScopeType.VARIANT)
                && (request.getTargetItems() == null || request.getTargetItems().isEmpty())) {
            throw new AppException(ErrorCode.PROMOTION_TARGET_ITEMS_REQUIRED);
        }

        if((request.getScopeType() == ScopeType.PRODUCT || request.getScopeType() == ScopeType.VARIANT)
            && request.getMinOrderAmount() != null) {
            throw new AppException(ErrorCode.PROMOTION_MIN_ORDER_AMOUNT_NOT_ALLOWED);
        }

        Promotion pr = promotionMapper.toPromotion(request);
        pr.setCreatedBy(currentUserService.getCurrentUser().getId());
        pr.setIsActive(request.getIsActive());

        if (pr.getTargetItems() == null) {
            pr.setTargetItems(new ArrayList<>());
        }

        if ((request.getScopeType() == ScopeType.PRODUCT || request.getScopeType() == ScopeType.VARIANT)
                && request.getTargetItems() != null) {
            List<PromotionTargetItem> targetItems = request.getTargetItems().stream()
                    .map(item -> PromotionTargetItem.builder()
                            .promotion(pr)
                            .targetId(item.getTargetId())
                            .targetType(item.getTargetType())
                            .build())
                    .toList();
            pr.getTargetItems().addAll(targetItems);
        }

        Promotion savedPromotion = promotionRepository.save(pr);

        if ((savedPromotion.getScopeType() == ScopeType.PRODUCT || savedPromotion.getScopeType() == ScopeType.VARIANT)
                && request.getTargetItems() != null) {
            syncByTargetItems.syncByTargetItems(request.getTargetItems());
        }

        return promotionMapper.toAdminPromotionResponse(savedPromotion);
    }

    public PageResponse<AdminPromotionResponse> getAll(Map<String, String> queryParams) {
        Page<Promotion> rawPromotionPage = promotionCoreService.getAll(queryParams, true);
        Page<AdminPromotionResponse> dtoPromotionRes = rawPromotionPage.map(promotionMapper::toAdminPromotionResponse);
        return PageResponse.of(dtoPromotionRes);
    }

    public AdminPromotionDetailResponse getPromotionById(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_EXISTED));

        return promotionMapper.toAdminPromotionDetailResponse(promotion);
    }

    public AdminPromotionResponse update(String promotionId, PromotionUpdateRequest request) {
        Promotion pr = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_EXISTED));

        if (request.getCode() != null && !request.getCode().equals(pr.getCode())) {
            if (promotionRepository.existsByCode(request.getCode())) {
                throw new AppException(ErrorCode.PROMOTION_EXISTED);
            }
        }

        if((request.getScopeType() == ScopeType.PRODUCT || request.getScopeType() == ScopeType.VARIANT)
                && (request.getTargetItems() == null || request.getTargetItems().isEmpty())) {
            throw new AppException(ErrorCode.PROMOTION_TARGET_ITEMS_REQUIRED);
        }

        promotionMapper.updatePromotionfromRequest(request, pr);

        List<TargetItemRequest> targetsToSync = new ArrayList<>();

        if (request.getTargetItems() != null) {
            if (pr.getTargetItems() != null) {
                targetsToSync.addAll(pr.getTargetItems().stream()
                        .map(item -> new TargetItemRequest(item.getTargetId(), item.getTargetType()))
                        .toList());
                pr.getTargetItems().clear();
            } else {
                pr.setTargetItems(new ArrayList<>());
            }

            List<PromotionTargetItem> newTargetItems = request.getTargetItems().stream()
                    .map(item -> PromotionTargetItem.builder()
                            .promotion(pr)
                            .targetId(item.getTargetId())
                            .targetType(item.getTargetType())
                            .build())
                    .toList();
            pr.getTargetItems().addAll(newTargetItems);

            targetsToSync.addAll(request.getTargetItems());
        }
        else if (pr.getTargetItems() != null) {
            targetsToSync.addAll(pr.getTargetItems().stream()
                    .map(item -> new TargetItemRequest(item.getTargetId(), item.getTargetType()))
                    .toList());
        }

        Promotion savedPromotion = promotionRepository.save(pr);

        if (savedPromotion.getScopeType() == ScopeType.PRODUCT || savedPromotion.getScopeType() == ScopeType.VARIANT) {
            if (!targetsToSync.isEmpty()) {
                syncByTargetItems.syncByTargetItems(targetsToSync);
            }
        }

        return promotionMapper.toAdminPromotionResponse(savedPromotion);
    }

    public void softDeletePromotion(String promotionId) {
        Promotion pr = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_EXISTED));
        pr.setDeletedBy(currentUserService.getCurrentUser().getId());
        pr.setIsActive(false);

        List<TargetItemRequest> targetsToSync = new ArrayList<>();
        if ((pr.getScopeType() == ScopeType.PRODUCT || pr.getScopeType() == ScopeType.VARIANT)
                && pr.getTargetItems() != null) {
            targetsToSync = pr.getTargetItems().stream()
                    .map(item -> new TargetItemRequest(item.getTargetId(), item.getTargetType()))
                    .toList();
        }

        promotionRepository.delete(pr);
        promotionRepository.flush();

        if (!targetsToSync.isEmpty()) {
            syncByTargetItems.syncByTargetItems(targetsToSync);
        }
    }

    public void restorePromotion(String promotionId) {
        int rowsAffected = promotionRepository.restorePromotion(promotionId);
        if (rowsAffected == 0) {
            throw new AppException(ErrorCode.PROMOTION_NOT_DELETED_OR_NOT_FOUND);
        }

        Promotion pr = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_EXISTED));

        if ((pr.getScopeType() == ScopeType.PRODUCT || pr.getScopeType() == ScopeType.VARIANT) && pr.getTargetItems() != null) {
            List<TargetItemRequest> targetsToSync = pr.getTargetItems().stream()
                    .map(item -> new TargetItemRequest(item.getTargetId(), item.getTargetType()))
                    .toList();

            if (!targetsToSync.isEmpty()) {
                syncByTargetItems.syncByTargetItems(targetsToSync);
            }
        }
    }

    public void hardDeletePromotion(String promotionId) {
        Promotion pr = promotionRepository.getPromotionById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_EXISTED));
        log.info("promotion to be hard deleted: {}", pr);

        List<TargetItemRequest> targetsToSync = new ArrayList<>();
        if ((pr.getScopeType() == ScopeType.PRODUCT || pr.getScopeType() == ScopeType.VARIANT)
                && pr.getTargetItems() != null) {
            targetsToSync = pr.getTargetItems().stream()
                    .map(item -> new TargetItemRequest(item.getTargetId(), item.getTargetType()))
                    .toList();
        }


        promotionTargetItemRepository.hardDelByPromotionId(pr.getId());

        promotionRepository.hardDelById(pr.getId());

        if (!targetsToSync.isEmpty()) {
            syncByTargetItems.syncByTargetItems(targetsToSync);
        }
    }

    @Transactional
    public void consumePromotionUsage(String promotionId) {
        int updatedRows = promotionRepository.incrementUsedCount(promotionId);
        if (updatedRows == 0) {
            throw new AppException(ErrorCode.PROMOTION_OUT_OF_USAGE);
        }

        Promotion promotion = promotionRepository.findById(promotionId).orElse(null);
        if (promotion != null && promotion.getUsedCount().equals(promotion.getUsageLimit())) {
            if (promotion.getTargetItems() != null && !promotion.getTargetItems().isEmpty()) {
                List<TargetItemRequest> targets = promotion.getTargetItems().stream()
                        .map(item -> new TargetItemRequest(item.getTargetId(), item.getTargetType()))
                        .toList();
                syncByTargetItems.syncByTargetItems(targets);
            }
        }
    }
}