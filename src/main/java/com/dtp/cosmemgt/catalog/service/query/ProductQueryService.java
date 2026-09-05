package com.dtp.cosmemgt.catalog.service.query;

import com.dtp.cosmemgt.catalog.dto.response.ProductCardResponse;
import com.dtp.cosmemgt.catalog.dto.response.ProductDetailResponse;
import com.dtp.cosmemgt.catalog.dto.response.ProductResponse;
import com.dtp.cosmemgt.catalog.dto.response.ProductSaleResponse;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.mapper.ProductMapper;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.core.utils.SlugUtils;
import com.dtp.cosmemgt.sales.promotion.entity.Promotion;
import com.dtp.cosmemgt.sales.promotion.entity.PromotionTargetItem;
import com.dtp.cosmemgt.sales.promotion.enums.DiscountType;
import com.dtp.cosmemgt.sales.promotion.repository.PromotionRepository;
import com.dtp.cosmemgt.sales.promotion.service.PromotionCalculator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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

    public ProductDetailResponse getProductBySlug(String slug){
        Product p = productRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        if(p.getDeletedAt()!=null)
            throw new AppException(ErrorCode.PRODUCT_UNAVAILABLE);

        return productMapper.toProductDetailResponse(p);
    }

    public PageResponse<ProductResponse> getAll(Map<String, String> queryParams) {
        Page<Product> rawProductPage = productCoreService.getAll(queryParams, false);

        Page<ProductResponse> dtoProductRes = rawProductPage.map(productMapper::toProductResponse);

        return PageResponse.of(dtoProductRes);
    }

    public List<ProductCardResponse> getTop12BestSellers() {
        Pageable topTwelve = PageRequest.of(0, 12);

        // Kết quả trả về là List các mảng Object, mỗi mảng có 2 phần tử: [Product, Long]
        List<Object[]> results = productRepository.findBestSellingProductsWithTotalSold(topTwelve);

        return results.stream()
                .map(record -> {
                    Product product = (Product) record[0];
                    Long totalSold = (Long) record[1];

                    // Ném Entity vào hàm dùng chung để tính toán khuyến mãi
                    return buildProductCard(product, totalSold);
                })
                .toList();
    }

    // Đối với API Hàng Khuyến mãi, không có tổng lượt bán thì truyền null hoặc 0L
    public List<ProductCardResponse> getTop12SaleProducts() {
        // Sửa lại theo Native Query chuẩn Limit 12 hôm trước
        List<Product> onSaleProducts = productRepository.findProductCurrentlyOnSale();

        return onSaleProducts.stream()
                .map(product -> buildProductCard(product, 0L)) // Mặc định lượt bán = 0
                .toList();
    }

    // Hàm lõi tái sử dụng (Entity -> DTO)
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
        // Long totalSold = tính tổng soldCount từ các variant nếu cần...

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