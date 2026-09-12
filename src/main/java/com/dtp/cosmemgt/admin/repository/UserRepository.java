package com.dtp.cosmemgt.admin.repository;

import com.dtp.cosmemgt.admin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, String> {//UUID → string
    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);
    Page<User> findByRoles_Name(String roleName, Pageable pageable);

    //statistic
    @Query("select count(u) from User u join u.roles r where r.name = :role")
    long countByRoleName(@Param("role") String roleName);
}
