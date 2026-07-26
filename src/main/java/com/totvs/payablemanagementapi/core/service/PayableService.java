package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.PayableNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableFilterDto;
import com.totvs.payablemanagementapi.core.port.input.dto.UpdatePayableStatusDto;
import com.totvs.payablemanagementapi.core.port.output.PayablePersistencePort;
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
    private final SupplierService supplierService;
    private final PayablePersistencePort payablePersistencePort;

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
        Supplier supplier = supplierService.findById(payableDto.supplierId());
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

        Supplier supplier = supplierService.findById(payableDto.supplierId());
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
    public Payable updateStatus(Long id, UpdatePayableStatusDto updatePayableStatusDto) {
        if (id == null) {
            throw new InvalidPayableException("O id da conta a pagar é obrigatório");
        }

        if (updatePayableStatusDto.status() == null) {
            throw new InvalidPayableException("O status da conta a pagar é obrigatório");
        }

        StatusPayableEnum status = StatusPayableEnum.fromCode(updatePayableStatusDto.status());

        Payable payable = findById(id);

        if (payable.getStatus().equals(status)
                && status != StatusPayableEnum.PAGO
                && updatePayableStatusDto.paymentDate() == null) {
            return payable;
        }

        payable.updateStatus(status, updatePayableStatusDto.paymentDate());
        return payablePersistencePort.save(payable);
    }
}
