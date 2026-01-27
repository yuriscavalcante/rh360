package com.rh360.rh360.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rh360.rh360.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    List<Permission> findByUserId(UUID userId);

    Optional<Permission> findByUserIdAndFunction(UUID userId, String function);

}
