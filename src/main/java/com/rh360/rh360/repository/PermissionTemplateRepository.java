package com.rh360.rh360.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.rh360.rh360.entity.PermissionTemplate;

@Repository
public interface PermissionTemplateRepository extends JpaRepository<PermissionTemplate, UUID> {

  @Query("SELECT pt FROM PermissionTemplate pt WHERE LOWER(pt.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(pt.label) LIKE LOWER(CONCAT('%', :search, '%'))")
  List<PermissionTemplate> findByNomeOrLabelContainingIgnoreCase(@Param("search") String search);
}
