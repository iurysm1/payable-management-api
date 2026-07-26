package com.totvs.payablemanagementapi.adapter.output.repository;

import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PayableRepository extends JpaRepository<Payable, Long> {

    @Query("""
            SELECT p
            FROM Payable p
              WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', COALESCE(:description, ''), '%'))
              AND (CAST(:startDate AS date) IS NULL
                   OR p.expirationDate BETWEEN :startDate AND :endDate)
              AND (:status IS NULL OR p.status = :status)
            """)
    Page<Payable> findAllByFilters(
            @Param("description") String description,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") StatusPayableEnum status,
            Pageable pageable
    );

    boolean existsBySupplierId(Long supplierId);
}
