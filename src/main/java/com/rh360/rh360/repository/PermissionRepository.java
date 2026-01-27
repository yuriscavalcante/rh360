package com.rh360.rh360.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.rh360.rh360.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    List<Permission> findByUserId(UUID userId);

    Optional<Permission> findByUserIdAndFunction(UUID userId, String function);

    @Query("SELECT p FROM Permission p WHERE LOWER(p.function) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Permission> findByFunctionContainingIgnoreCase(@Param("search") String search, Pageable pageable);

}
