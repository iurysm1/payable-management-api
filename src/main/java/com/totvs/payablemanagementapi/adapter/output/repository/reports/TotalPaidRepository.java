package com.totvs.payablemanagementapi.adapter.output.repository.reports;

import com.totvs.payablemanagementapi.core.port.input.dto.reports.PaidPayableItemDto;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TotalPaidRepository extends JpaRepository<Payable, Long> {

    @Query("""
            SELECT new com.totvs.payablemanagementapi.core.port.input.dto.reports.PaidPayableItemDto(
                p.description,
                p.amount,
                p.status,
                p.expirationDate,
                p.paymentDate,
                p.supplier.name
            )
            FROM Payable p
            WHERE p.paymentDate BETWEEN :startDate AND :endDate
              AND p.status = :status
              AND (:supplierId IS NULL OR p.supplier.id = :supplierId)
            """)
    List<PaidPayableItemDto> findPaidByPaymentDatePeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") StatusPayableEnum status,
            @Param("supplierId") Long supplierId
    );
}
