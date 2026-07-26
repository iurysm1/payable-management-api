package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.port.input.PayableImportationServiceUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableImportationDto;
import com.totvs.payablemanagementapi.core.port.output.FileStoragePort;
import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEventPublisher;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PayableImportationService implements PayableImportationServiceUseCase {

    private final PayableImportPersistencePort payableImportPersistencePort;
    private final PayableImportEventPublisher payableImportEventPublisher;


    @Override
    public PayableImportation create() {
        PayableImportation payableImportation = PayableImportation.create();

        payableImportPersistencePort.create(payableImportation);
        payableImportEventPublisher.publish()

        return payableImportation;
    }

    @Override
    public PayableImportation findById(Long id) {
        return null;
    }

    @Override
    public List<PayableImportation> list() {
        return List.of();
    }

    @Override
    public List<PayableImportationItem> listPayableImportationItem(long id) {
        return List.of();
    }
}
