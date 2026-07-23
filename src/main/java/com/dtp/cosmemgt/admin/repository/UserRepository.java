package com.dtp.cosmemgt.admin.repository;

import com.dtp.cosmemgt.admin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, String> {//UUID → string
    Optional<User> findByEmail(String email);
}
