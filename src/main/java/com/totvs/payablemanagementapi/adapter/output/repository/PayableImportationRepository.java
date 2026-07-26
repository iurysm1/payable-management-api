package com.totvs.payablemanagementapi.adapter.output.repository;

import com.totvs.payablemanagementapi.domain.PayableImportation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayableImportationRepository extends JpaRepository<PayableImportation, Long> {
}
