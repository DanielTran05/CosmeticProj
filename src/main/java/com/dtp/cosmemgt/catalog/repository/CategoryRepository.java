package com.dtp.cosmemgt.catalog.repository;

import com.dtp.cosmemgt.catalog.dto.response.CustomerCategoryResponse;
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

    @Query("select new com.dtp.cosmemgt.catalog.dto.response.CustomerCategoryResponse(" +
            "c.id, c.parentCategory.id, c.name, c.img, count(p)) " +
            "from Category c " +
            "left join Product p on p.category = c AND p.deletedAt IS NULL " +
            "where c.deletedAt is null " +
            "group by c.id, c.parentCategory.id, c.name, c.img")
//    @Query("SELECT new com.dtp.cosmemgt.catalog.dto.response.CustomerCategoryResponse(" +
//            "c.id, c.parentCategory.id, c.name, c.img, COUNT(p)) " +
//            "FROM Category c " +
//            "LEFT JOIN Product p ON p.category = c AND p.deletedAt IS NULL " +
//            "WHERE c.deletedAt IS NULL " +
//            "GROUP BY c.id, c.parentCategory.id, c.name, c.img")
    List<CustomerCategoryResponse> findAllCustomerCate();

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