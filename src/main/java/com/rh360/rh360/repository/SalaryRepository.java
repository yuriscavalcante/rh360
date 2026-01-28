package com.rh360.rh360.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rh360.rh360.entity.Salary;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, UUID> {

    List<Salary> findByUser_IdAndDeletedAtIsNullOrderByReferenceMonthDesc(UUID userId);

    List<Salary> findByUser_IdAndReferenceMonthAndDeletedAtIsNull(UUID userId, String referenceMonth);

    @Query("""
        SELECT s FROM Salary s
        WHERE s.user.id = :userId
          AND s.deletedAt IS NULL
          AND (:fromPaidAt IS NULL OR s.paidAt >= :fromPaidAt)
          AND (:toPaidAt IS NULL OR s.paidAt <= :toPaidAt)
        ORDER BY s.paidAt DESC
    """)
    List<Salary> findByUserIdAndPaidAtRange(
            @Param("userId") UUID userId,
            @Param("fromPaidAt") LocalDate fromPaidAt,
            @Param("toPaidAt") LocalDate toPaidAt);
}

