package com.totvs.payablemanagementapi.adapter.output.repository;

import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PayableImportationJpaAdapter implements PayableImportPersistencePort {

    private final PayableImportationRepository payableImportationRepository;
    private final PayableImportationItemRepository payableImportationItemRepository;

    @Override
    public PayableImportation save(PayableImportation payableImportation) {
        return payableImportationRepository.save(payableImportation);
    }

    @Override
    public PayableImportation findById(long id) {
        return payableImportationRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(long id) {
        payableImportationRepository.deleteById(id);
    }

    @Override
    public List<PayableImportation> findAll() {
        return payableImportationRepository.findAll();
    }

    @Override
    public List<PayableImportationItem> findByPayableImportationId(long id) {
        return payableImportationItemRepository.findByPayableImportationId(id);
    }
}
