package com.dtp.cosmemgt.catalog.repository;

import com.dtp.cosmemgt.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByName(String name);

    @Modifying
    @Query(value = "delete from category where id = ?1", nativeQuery = true)
    void hardDelById(int id);

    @Modifying(clearAutomatically = true)
    @Query(value = "update category set deleted_at = NULL where id = ?1", nativeQuery = true)
    int restoreById(int id);

    @Query(value = "select * from category where id = ?1", nativeQuery = true)
    Optional<Category> getById(int id);

    @Query(value = "select * from category", nativeQuery = true)
    List<Category> findAllForAdmin();
}