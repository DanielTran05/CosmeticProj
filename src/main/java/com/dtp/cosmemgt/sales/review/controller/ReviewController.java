package com.dtp.cosmemgt.sales.review.controller;

import com.dtp.cosmemgt.sales.review.dto.request.ReviewUpdateRequest;
import com.dtp.cosmemgt.sales.review.dto.response.ReviewResponseRecord;
import com.dtp.cosmemgt.sales.review.service.ReviewService;
import com.dtp.cosmemgt.sales.review.dto.request.ReviewCreationRequest;
import com.dtp.cosmemgt.sales.review.dto.response.ReviewResponse;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewController {
    ReviewService reviewService;

    @PostMapping()
    ApiResponse<ReviewResponse> create(@RequestBody ReviewCreationRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.create(request))
                .build();
    }

    @GetMapping("/{reviewId}")
    ApiResponse<ReviewResponse> getReviewDetail(@PathVariable int reviewId) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.getReviewDetail(reviewId))
                .build();
    }

    @GetMapping("/products/{productId}")
    ApiResponse<PageResponse<ReviewResponse>> getReviewsByProductId(@PathVariable String productId,
                                                                          @RequestParam Map<String, String> queryParams) {
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .result(reviewService.getReviewsByProductId(productId, queryParams))
                .build();
    }

    @PutMapping("/{reviewId}")
    ApiResponse<ReviewResponse> updateReview(@PathVariable int reviewId,
                                                     @RequestBody ReviewUpdateRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.update(reviewId, request))
                .build();
    }

    @DeleteMapping("/{reviewId}")
    ApiResponse<Void> delReview(@PathVariable int reviewId) {
        reviewService.delReview(reviewId);
        return ApiResponse.<Void>builder()
                .message("Review with id " + reviewId + " has been hard deleted successfully.")
                .build();
    }

    @GetMapping()
    ApiResponse<PageResponse<ReviewResponse>> getAll(@RequestParam Map<String, String> queryParams) {
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .result(reviewService.getAll(queryParams))
                .build();
    }
}