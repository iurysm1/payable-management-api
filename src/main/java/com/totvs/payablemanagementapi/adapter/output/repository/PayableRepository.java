package com.totvs.payablemanagementapi.adapter.output.repository;

import com.totvs.payablemanagementapi.domain.Payable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayableRepository extends JpaRepository<Payable, Long> {
}
