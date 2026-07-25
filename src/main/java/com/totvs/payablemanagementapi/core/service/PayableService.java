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
        // Verificar se payable.amount é nulo ou menor que zero, se for lancar uma badrequest exeception
        // Criar um metodo para isso no Domain, pois vai ser usado em update tambem
        // Criar uma personalizada para payables
        return payablePersistencePort.save(payable);
    }

    @Override
    public Payable update(Payable payable) {
        Payable existingPayable = findById(payable.getId());
        // Verificar se amount eh nulo ou maior que 0
        existingPayable.setDescription(payable.getDescription());
        existingPayable.setAmount(payable.getAmount());
        existingPayable.setStatus(payable.getStatus());
        existingPayable.setExpirationDate(payable.getExpirationDate());
        existingPayable.setPaymentDate(payable.getPaymentDate());
        existingPayable.setSupplier(payable.getSupplier());

        return payablePersistencePort.save(existingPayable);
    }

    @Override
    public void delete(Long id) {
        Payable payable = findById(id);
        payablePersistencePort.delete(payable);
    }
}
