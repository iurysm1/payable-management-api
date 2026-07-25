package com.totvs.payablemanagementapi.core.port.input;

import com.totvs.payablemanagementapi.core.port.input.dto.SupplierDto;
import com.totvs.payablemanagementapi.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierUseCase {

    Page<Supplier> list(Pageable pageable);

    Supplier findById(Long id);

    Supplier save(SupplierDto supplierDto);

    Supplier update(SupplierDto supplierDto);

    void delete(Long id);
}
