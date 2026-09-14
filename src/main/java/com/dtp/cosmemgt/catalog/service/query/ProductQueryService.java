package com.dtp.cosmemgt.catalog.service.query;

import com.dtp.cosmemgt.catalog.dto.response.*;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.mapper.ProductMapper;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import com.dtp.cosmemgt.sales.promotion.entity.PromotionTargetItem;
import com.dtp.cosmemgt.sales.promotion.enums.DiscountType;
import com.dtp.cosmemgt.sales.promotion.repository.PromotionRepository;
import com.dtp.cosmemgt.sales.promotion.service.PromotionCalculator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class ProductQueryService {
    private final PromotionRepository promotionRepository;
    ProductCoreService productCoreService;
    PromotionCalculator promotionCalculator;
    ProductMapper productMapper;
    ProductRepository productRepository;
    RedisTemplate<String, Object> redisTemplate;

    @Cacheable(
            value = "Products",
            key = "'slug_' + #slug",
//            unless = "#result == null",
            sync = true
    )
    public ProductDetailResponse getProductBySlug(String slug){
        String cacheKey = "products::" + slug;
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (cachedData != null) {
            if ("NULL_VALUE".equals(cachedData))
                return null;
            return (ProductDetailResponse) cachedData;
        }

        Product p = productRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        if (p.getDeletedAt() != null)
            throw new AppException(ErrorCode.PRODUCT_UNAVAILABLE);

        ProductDetailResponse response = productMapper.toProductDetailResponse(p);

        long finalTtl = 10 + new Random().nextInt(4);
        redisTemplate.opsForValue().set(cacheKey, response, finalTtl, TimeUnit.MINUTES);

        return response;
    }

    @Cacheable(
            value = "Products",
            condition = "#queryParams.get('keyword') == null " +
                    "&& #queryParams.get('cateId') == null " +
                    "&& #queryParams.get('sort') == null " +
                    "&& #queryParams.get('minPrice') == null " +
                    "&& #queryParams.get('maxPrice') == null",
            key = "'default_p_' + (#queryParams['page'] ?: '0') + '_s_' + (#queryParams['size'] ?: '10')",
            unless = "#result == null || #result.content == null || #result.content.isEmpty()"
    )
    public PageResponse<ProductResponse> getAll(Map<String, String> queryParams) {
        Page<Product> rawProductPage = productCoreService.getAll(queryParams, false);
        Page<ProductResponse> dtoProductRes = rawProductPage.map(productMapper::toProductResponse);
        return PageResponse.of(dtoProductRes);
    }

    @Cacheable(
            value = "Products",
            key = "'top12_bestsellers'",
            sync = true
    )
    public ProductCardListResponse getTop12BestSellers() {
        List<BestSellerProductProjection> results = productRepository.findTop12BestSellingProducts();

        List<ProductCardResponse> cards = results.stream()
                .map(record -> buildProductCard(record.getProduct(), record.getTotalSold()))
                .toList();

        return ProductCardListResponse.of(cards);
    }

    @Cacheable(
            value = "Products",
            key = "'top12_saleproduct'",
            sync = true
    )
    public ProductCardListResponse getTop12SaleProducts() {
        List<Product> onSaleProducts = productRepository.findProductCurrentlyOnSale();

        List<ProductCardResponse> cards = onSaleProducts.stream()
                .map(product -> buildProductCard(product, 0L))
                .toList();

        return ProductCardListResponse.of(cards);
    }

    private ProductCardResponse buildProductCard(Product product, Long totalSold) {
        List<String> variantIds = product.getProductVariants().stream()
                .map(ProductVariant::getId)
                .toList();

        List<Promotion> activePromotions = promotionRepository.findActiveProductPromotions(
                product.getId(), variantIds, LocalDateTime.now()
        );

        BigDecimal minOriginalPrice = null;
        BigDecimal minDiscountedPrice = null;
        String bestBadge = "";
        String thumbnail = "";

        for (ProductVariant variant : product.getProductVariants()) {
            BigDecimal original = variant.getUnitPrice();
            BigDecimal currentDiscounted = original;
            Promotion bestPromo = null;

            for (Promotion promo : activePromotions) {
                boolean isApplicable = promo.getTargetItems().stream()
                        .map(PromotionTargetItem::getTargetId)
                        .anyMatch(targetId -> targetId.equals(product.getId()) || targetId.equals(variant.getId()));

                if (isApplicable) {
                    BigDecimal calculatedPrice = promotionCalculator.calculateDiscountedPrice(original, promo);
                    if (calculatedPrice.compareTo(currentDiscounted) < 0) {
                        currentDiscounted = calculatedPrice;
                        bestPromo = promo;
                    }
                }
            }

            if (minDiscountedPrice == null || currentDiscounted.compareTo(minDiscountedPrice) < 0) {
                minDiscountedPrice = currentDiscounted;
                minOriginalPrice = original;
                thumbnail = variant.getImg();

                if (bestPromo != null) {
                    bestBadge = bestPromo.getDiscountType() == DiscountType.PERCENT
                            ? "Giảm " + bestPromo.getDiscountValue() + "%"
                            : "Giảm " + bestPromo.getDiscountValue() + "đ";
                }
            }
        }

        return new ProductCardResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                thumbnail,
                minOriginalPrice,
                minDiscountedPrice,
                bestBadge,
                totalSold
        );
    }
}