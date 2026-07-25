package com.totvs.payablemanagementapi.core.port.input;

import com.totvs.payablemanagementapi.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierUseCase {

    Page<Supplier> list(Pageable pageable);

    Supplier findById(Long id);

    Supplier save(Supplier supplier);

    Supplier update(Supplier supplier);

    void delete(Long id);
}
