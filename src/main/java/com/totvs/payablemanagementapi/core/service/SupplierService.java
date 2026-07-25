package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.SupplierInUseException;
import com.totvs.payablemanagementapi.core.exception.SupplierNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.SupplierUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.SupplierDto;
import com.totvs.payablemanagementapi.core.port.output.PayablePersistencePort;
import com.totvs.payablemanagementapi.core.port.output.SupplierPersistencePort;
import com.totvs.payablemanagementapi.domain.Supplier;
import com.totvs.payablemanagementapi.domain.exception.InvalidSupplierException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupplierService implements SupplierUseCase {

    private final SupplierPersistencePort supplierPersistencePort;
    private final PayablePersistencePort payablePersistencePort;

    @Override
    public Page<Supplier> list(Pageable pageable) {
        return supplierPersistencePort.findAll(pageable);
    }

    @Override
    public Supplier findById(Long id) {
        if (id == null) {
            throw new InvalidSupplierException("O id do fornecedor é obrigatório");
        }

        return supplierPersistencePort.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
    }

    @Override
    public Supplier save(SupplierDto supplierDto) {
        Supplier supplier = Supplier.create(supplierDto.name());
        return supplierPersistencePort.save(supplier);
    }

    @Override
    public Supplier update(SupplierDto supplierDto) {
        Supplier existingSupplier = findById(supplierDto.id());
        existingSupplier.updateDetails(supplierDto.name());

        return supplierPersistencePort.save(existingSupplier);
    }

    @Override
    public void delete(Long id) {
        Supplier supplier = findById(id);

        if (payablePersistencePort.existsBySupplierId(id)) {
            throw new SupplierInUseException(id);
        }

        supplierPersistencePort.delete(supplier);
    }
}
