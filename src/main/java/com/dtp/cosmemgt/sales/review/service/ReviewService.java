package com.dtp.cosmemgt.sales.review.service;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.admin.repository.UserRepository;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.repository.ProductRepository;
import com.dtp.cosmemgt.catalog.repository.ProductVariantRepository;
import com.dtp.cosmemgt.core.commonService.CurrentUserService;
import com.dtp.cosmemgt.sales.review.dto.request.ReviewUpdateRequest;
import com.dtp.cosmemgt.sales.review.dto.response.ReviewResponseRecord;
import com.dtp.cosmemgt.sales.review.entity.Review;
import com.dtp.cosmemgt.sales.review.dto.request.ReviewCreationRequest;
import com.dtp.cosmemgt.sales.review.dto.response.ReviewResponse;
import com.dtp.cosmemgt.sales.review.mapper.ReviewMapper;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.core.dto.PageResponse;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.sales.order.repository.OrderRepository;
import com.dtp.cosmemgt.sales.review.repository.ReviewRepository;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class ReviewService {
    CurrentUserService currentUserService;

    ReviewRepository reviewRepository;
    OrderRepository orderRepository;
    ProductRepository productRepository;

    ReviewMapper reviewMapper;

    public ReviewResponse create(ReviewCreationRequest request) {
        Product p = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        User u = currentUserService.getCurrentUser();

        if(!orderRepository.hasUserPurchasedAnyVariantOfProduct(u.getId(), p.getId()))
            throw new AppException(ErrorCode.HAS_NOT_USED_YET);

        if(reviewRepository.existsByCustomerIdAndProductId(u.getId(), p.getId()))
            throw new AppException(ErrorCode.ONLY_ONE_REVIEW_FOR_CUS_PRODUCT);

        Review r = reviewMapper.toReview(request);
        r.setCustomer(u);
        r.setProduct(p);
        r.setRatingStar(request.getRatingStar());
        r.setComment(request.getComment());

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

    public PageResponse<ReviewResponse> getReviewsByProductId(String productId, Map<String, String> queryParams) {
        int page = queryParams.containsKey("page") ? Integer.parseInt(queryParams.get("page")) : 0;
        int size = queryParams.containsKey("size") ? Integer.parseInt(queryParams.get("size")) : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Review> reviewsPage = reviewRepository.findReviewsByProductId(productId, pageable);
        Page<ReviewResponse> reviewsPageResponse = reviewsPage.map(reviewMapper::toReviewResponse);
        return PageResponse.of(reviewsPageResponse);
    }

    public ReviewResponse getReviewDetail(int reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));
        return reviewMapper.toReviewResponse(r);
    }

    public ReviewResponse update(int reviewId, ReviewUpdateRequest request){
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));

        User u = currentUserService.getCurrentUser();
        if (!r.getCustomer().getId().equals(u.getId())) {
            throw new AppException(ErrorCode.ORDER_DO_NOT_BELONG);
        }

        reviewMapper.updateReviewFromRequest(request, r);
        return reviewMapper.toReviewResponse(reviewRepository.save(r));
    }

    public void delReview(int reviewId) {
        User u = currentUserService.getCurrentUser();

        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));

        if (!r.getCustomer().getId().equals(u.getId())) {
            throw new AppException(ErrorCode.USER_HAS_NOT_REVIEWED_THIS_PRODUCT);
        }

        reviewRepository.delete(r);
    }
}