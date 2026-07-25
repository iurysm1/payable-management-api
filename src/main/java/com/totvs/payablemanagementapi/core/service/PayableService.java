package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.PayableNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.core.port.output.PayablePersistencePort;
import com.totvs.payablemanagementapi.domain.Payable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PayableService implements PayableUseCase {
    private final PayablePersistencePort payablePersistencePort;

    @Override
    public Page<Payable> list(Pageable pageable) {
        return payablePersistencePort.findAll(pageable);
    }

    @Override
    public Payable findById(Long id) {
        return payablePersistencePort.findById(id)
                .orElseThrow(() -> new PayableNotFoundException(id));
    }

    @Override
    public Payable save(Payable payable) {
        return payablePersistencePort.save(payable);
    }

    @Override
    public Payable update(Payable payable) {
        findById(payable.getId());

        return payablePersistencePort.save(payable);
    }

    @Override
    public void delete(Long id) {
        Payable payable = findById(id);
        payablePersistencePort.delete(payable);
    }
}
