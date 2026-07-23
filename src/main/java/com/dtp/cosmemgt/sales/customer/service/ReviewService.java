package com.dtp.cosmemgt.sales.customer.service;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.admin.repository.UserRepository;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.sales.customer.dto.request.ReviewUpdateRequest;
import com.dtp.cosmemgt.sales.entity.Review;
import com.dtp.cosmemgt.sales.customer.dto.request.ReviewCreationRequest;
import com.dtp.cosmemgt.sales.customer.dto.response.ReviewResponse;
import com.dtp.cosmemgt.sales.customer.mapper.ReviewMapper;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.catalog.entity.UnitOfMeasure;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.repository.OrderRepository;
import com.dtp.cosmemgt.sales.repository.ReviewRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class ReviewService {
    ReviewRepository reviewRepository;
    ProductVariantRepository productVariantRepository;
    UserRepository userRepository;
    OrderRepository orderRepository;

    ReviewMapper reviewMapper;

    public ReviewResponse create(ReviewCreationRequest request) {
        ProductVariant pv = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        User u = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!u.getId().equals(userId))
            throw new AppException(ErrorCode.ORDER_DO_NOT_BELONG);

        if(!orderRepository.hasUserPurchasedProduct(userId, pv.getId()))
            throw new AppException(ErrorCode.HAS_NOT_USED_YET);

        if(reviewRepository.existsByCustomerAndProductVariant(u, pv))
            throw new AppException(ErrorCode.ONLY_ONE_REVIEW_FOR_CUS_PV);

        Review r = reviewMapper.toReview(request);
        r.setCustomer(u);
        r.setProductVariant(pv);

        return reviewMapper.toReviewResponse(reviewRepository.save(r));
    }

    public PageResponse<ReviewResponse> getAll(Map<String, String> queryParams) {
        int page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 0;
        int size = queryParams.containsKey("size") ? Integer.parseInt(queryParams.get("size")) : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Review> pvs = reviewRepository.findAll(pageable);
        Page<ReviewResponse> pvResponse = pvs.map(reviewMapper::toReviewResponse);

        return PageResponse.of(pvResponse);
    }

    public ReviewResponse getReviewById(int reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));
        return reviewMapper.toReviewResponse(r);
    }

    public ReviewResponse update(int reviewId, ReviewUpdateRequest request){
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));

        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!r.getCustomer().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ORDER_DO_NOT_BELONG);
        }

        reviewMapper.updateReviewFromRequest(request, r);
        return reviewMapper.toReviewResponse(reviewRepository.save(r));
    }

    public void delReview(int reviewId) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));

        if (!r.getCustomer().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.USER_HAS_NOT_REVIEWED_THIS_PRODUCT); // Hoặc mã lỗi khác phù hợp
        }

        reviewRepository.delete(r);
    }
}