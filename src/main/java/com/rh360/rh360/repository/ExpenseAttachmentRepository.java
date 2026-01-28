package com.rh360.rh360.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rh360.rh360.entity.ExpenseAttachment;

@Repository
public interface ExpenseAttachmentRepository extends JpaRepository<ExpenseAttachment, UUID> {
    List<ExpenseAttachment> findByExpense_IdAndDeletedAtIsNull(UUID expenseId);
}

