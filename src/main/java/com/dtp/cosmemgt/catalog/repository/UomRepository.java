package com.dtp.cosmemgt.catalog.repository;

import com.dtp.cosmemgt.catalog.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Repository
public interface UomRepository extends JpaRepository<UnitOfMeasure, Integer> {
    boolean existsByName(String name);

    @Modifying
    @Query(value = "delete from uom where id = ?1", nativeQuery = true)
    void hardDelById(int id);

    @Modifying(clearAutomatically = true)
    @Query(value = "update uom set deleted_at = NULL where id = ?1", nativeQuery = true)
    int restoreById(int id);

    @Query(value = "select * from uom where id = ?1", nativeQuery = true)
    Optional<UnitOfMeasure> getById(int id);
}