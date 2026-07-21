package com.dtp.cosmemgt.catalog.repository;

import com.dtp.cosmemgt.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Repository
public interface ProductRepository extends JpaRepository<Product, String>,
                                        JpaSpecificationExecutor<Product> {
    boolean existsByName(String name);

    @Modifying
    @Query(value = "delete from product where id = ?1", nativeQuery = true)
    void hardDelById(String id);

    @Modifying(clearAutomatically = true)
    @Query(value = "update product set deleted_at = NULL where id = ?1", nativeQuery = true)
    int restoreById(String id);

    @Query(value = "select * from product where id = ?1", nativeQuery = true)
    Optional<Product> getProductById(String id);
}