package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.PayableNotFoundException;
import com.totvs.payablemanagementapi.core.exception.SupplierNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableFilterDto;
import com.totvs.payablemanagementapi.core.port.output.PayablePersistencePort;
import com.totvs.payablemanagementapi.core.port.output.SupplierPersistencePort;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.Supplier;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PayableService implements PayableUseCase {
    private final PayablePersistencePort payablePersistencePort;
    private final SupplierPersistencePort supplierPersistencePort;

    @Override
    public Page<Payable> list(Pageable pageable, PayableFilterDto filter) {
        return payablePersistencePort.findAll(pageable, filter);
    }

    @Override
    public Payable findById(Long id) {
        if (id == null) {
            throw new InvalidPayableException("O id da conta a pagar é obrigatório");
        }

        return payablePersistencePort.findById(id)
                .orElseThrow(() -> new PayableNotFoundException(id));
    }

    @Override
    public Payable save(PayableDto payableDto) {
        Supplier supplier = resolveSupplier(payableDto.supplierId());
        Payable payable = Payable.create(
                payableDto.description(),
                payableDto.amount(),
                payableDto.status(),
                payableDto.expirationDate(),
                payableDto.paymentDate(),
                supplier
        );

        return payablePersistencePort.save(payable);
    }

    @Override
    public Payable update(PayableDto payableDto) {
        if (payableDto.id() == null) {
            throw new InvalidPayableException("O id da conta a pagar é obrigatório");
        }

        Payable existingPayable = findById(payableDto.id());

        Supplier supplier = resolveSupplier(payableDto.supplierId());
        existingPayable.updateDetails(
                payableDto.description(),
                payableDto.amount(),
                payableDto.status(),
                payableDto.expirationDate(),
                payableDto.paymentDate(),
                supplier
        );

        return payablePersistencePort.save(existingPayable);
    }

    @Override
    public void delete(Long id) {
        Payable payable = findById(id);
        payablePersistencePort.delete(payable);
    }

    @Override
    public Payable updateStatus(Long id, StatusPayableEnum status) {
        if (id == null) {
            throw new InvalidPayableException("O id da conta a pagar é obrigatório");
        }

        Payable payable = findById(id);

        if (payable.getStatus().equals(status)) {
            return payable;
        }

        payable.updateStatus(status);
        return payablePersistencePort.save(payable);
    }

    private Supplier resolveSupplier(Long supplierId) {
        if (supplierId == null) {
            throw new InvalidPayableException("O fornecedor da conta a pagar é obrigatório");
        }

        return supplierPersistencePort.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));
    }
}
