package com.rh360.rh360.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rh360.rh360.entity.CompanyExpense;

@Repository
public interface CompanyExpenseRepository extends JpaRepository<CompanyExpense, UUID> {

    @Query("""
        SELECT e FROM CompanyExpense e
        WHERE e.deletedAt IS NULL
          AND (:fromDate IS NULL OR e.date >= :fromDate)
          AND (:toDate IS NULL OR e.date <= :toDate)
          AND (:type IS NULL OR LOWER(e.type) = LOWER(:type))
        ORDER BY e.date DESC
    """)
    List<CompanyExpense> findByFilters(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("type") String type);
}

