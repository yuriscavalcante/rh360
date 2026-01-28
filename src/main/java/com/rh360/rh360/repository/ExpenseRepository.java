package com.rh360.rh360.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rh360.rh360.entity.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByUser_IdAndDeletedAtIsNullOrderByDateDesc(UUID userId);

    @Query("""
        SELECT e FROM Expense e
        WHERE e.user.id = :userId
          AND e.deletedAt IS NULL
          AND (:fromDate IS NULL OR e.date >= :fromDate)
          AND (:toDate IS NULL OR e.date <= :toDate)
          AND (:category IS NULL OR LOWER(e.category) = LOWER(:category))
        ORDER BY e.date DESC
    """)
    List<Expense> findByUserIdAndFilters(
            @Param("userId") UUID userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("category") String category);
}

