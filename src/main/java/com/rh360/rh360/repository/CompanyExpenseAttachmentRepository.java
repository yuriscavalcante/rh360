package com.rh360.rh360.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rh360.rh360.entity.CompanyExpenseAttachment;

@Repository
public interface CompanyExpenseAttachmentRepository extends JpaRepository<CompanyExpenseAttachment, UUID> {
    List<CompanyExpenseAttachment> findByCompanyExpense_IdAndDeletedAtIsNull(UUID companyExpenseId);
}

