package com.dtp.cosmemgt.catalog.repository;

import com.dtp.cosmemgt.catalog.dto.response.BestSellerResponse;
import com.dtp.cosmemgt.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface ProductRepository extends JpaRepository<Product, String>,
                                        JpaSpecificationExecutor<Product> {
    boolean existsByName(String name);

    @Query(value = "select p from Product p left join fetch p.productVariants where p.id = :productId")
    Optional<Product> findByIdWithVariants(String productId);

    @Modifying
    @Query(value = "delete from product where id = ?1", nativeQuery = true)
    void hardDelById(String id);

    @Modifying(clearAutomatically = true)
    @Query(value = "update product set deleted_at = NULL where id = ?1", nativeQuery = true)
    int restoreById(String id);

    @Query(value = "select * from product where id = ?1", nativeQuery = true)
    Optional<Product> getProductById(String id);

    @Query("""
    SELECT new com.dtp.cosmemgt.catalog.dto.response.BestSellerResponse(
        pv.id, 
        p.name, 
        pv.variantName, 
        SUM(od.quantity)
    )
    FROM OrderDetail od
    JOIN od.productVariant pv
    JOIN pv.product p
    JOIN od.order o
    WHERE o.orderStatus = 'CONFIRMED'
    GROUP BY pv.id, p.name, pv.variantName
    ORDER BY SUM(od.quantity) DESC
    """)
    List<BestSellerResponse> findBestSellingVariant(Pageable pageable);
}