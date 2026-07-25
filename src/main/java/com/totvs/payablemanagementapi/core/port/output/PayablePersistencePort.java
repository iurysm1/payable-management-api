package com.totvs.payablemanagementapi.core.port.output;

import com.totvs.payablemanagementapi.domain.Payable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PayablePersistencePort {
    Page<Payable> findAll(Pageable pageable);
    Optional<Payable> findById(Long id);
    Payable save(Payable payable);
    void delete(Payable payable);
    boolean existsBySupplierId(Long supplierId);
}
