package com.totvs.payablemanagementapi.core.port.output;

import com.totvs.payablemanagementapi.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SupplierPersistencePort {

    Page<Supplier> findAll(Pageable pageable);

    Optional<Supplier> findById(Long id);

    Supplier save(Supplier supplier);

    void delete(Supplier supplier);
}
