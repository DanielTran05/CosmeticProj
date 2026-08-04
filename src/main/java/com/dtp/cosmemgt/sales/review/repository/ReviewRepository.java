package com.dtp.cosmemgt.sales.review.repository;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import com.dtp.cosmemgt.sales.review.dto.response.ReviewResponseRecord;
import com.dtp.cosmemgt.sales.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @Modifying
    @Query(value = "delete from review where id = ?1", nativeQuery = true)
    void hardDelById(int id);

    @Modifying(clearAutomatically = true)
    @Query(value = "update review set deleted_at = NULL where id = ?1", nativeQuery = true)
    int restoreById(int id);

    @Query(value = "select * from review where id = ?1", nativeQuery = true)
    Optional<Review> getById(int id);

    boolean existsByCustomerAndProductVariant(User customer, ProductVariant productVariant);

    boolean existsByCustomer(User customer);

    @Query("""
        select new ReviewResponseRecord(
            r.id, 
            u.fullName, 
            r.rating, 
            r.content, 
            v.variantName
        )
        from Review r 
        join r.user u
        join r.productVariant v
        where r.product.id = :productId
        order BY r.createdAt DESC
        """)
    Page<ReviewResponseRecord> findReviewsByProductId(String productId, Pageable pageable);
}