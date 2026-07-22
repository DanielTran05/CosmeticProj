package com.dtp.cosmemgt.catalog.repository;

import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String>, JpaSpecificationExecutor<ProductVariant> {
    boolean existsByVariantName(String variantName);

    List<ProductVariant> findAllByProductId(String productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM product_variant WHERE id = :id", nativeQuery = true)
    void hardDelById(@Param("id") String id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE product_variant SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") String id);

    @Query(value = "SELECT * FROM product_variant WHERE id = :id", nativeQuery = true)
    Optional<ProductVariant> findByIdForAdmin(@Param("id") String id);
}