package com.rh360.rh360.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rh360.rh360.entity.PermissionTemplate;

@Repository
public interface PermissionTemplateRepository extends JpaRepository<PermissionTemplate, UUID> {

}
