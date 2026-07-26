package com.totvs.payablemanagementapi.adapter.output.repository;

import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayableImportationItemRepository extends JpaRepository<PayableImportationItem, Long> {
    List<PayableImportationItem> findByPayableImportationId(Long payableImportationId);
}
