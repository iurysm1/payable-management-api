package com.totvs.payablemanagementapi.adapter.output.repository;

import com.totvs.payablemanagementapi.core.port.input.dto.PayableFilterDto;
import com.totvs.payablemanagementapi.core.port.output.PayablePersistencePort;
import com.totvs.payablemanagementapi.domain.Payable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PayableJpaAdapter implements PayablePersistencePort {

    private final PayableRepository payableRepository;

    @Override
    public Page<Payable> findAll(Pageable pageable, PayableFilterDto filter) {
        return payableRepository.findAllByFilters(
                filter.description(),
                filter.periodCriteria().startDate(),
                filter.periodCriteria().endDate(),
                pageable
        );
    }

    @Override
    public Optional<Payable> findById(Long id) {
        return payableRepository.findById(id);
    }

    @Override
    public Payable save(Payable payable) {
        return payableRepository.save(payable);
    }

    @Override
    public void delete(Payable payable) {
        payableRepository.delete(payable);
    }

    @Override
    public boolean existsBySupplierId(Long supplierId) {
        return payableRepository.existsBySupplierId(supplierId);
    }
}
