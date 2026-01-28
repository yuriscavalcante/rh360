package com.rh360.rh360.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rh360.rh360.entity.SalaryAttachment;

@Repository
public interface SalaryAttachmentRepository extends JpaRepository<SalaryAttachment, UUID> {
    List<SalaryAttachment> findBySalary_IdAndDeletedAtIsNull(UUID salaryId);
}

