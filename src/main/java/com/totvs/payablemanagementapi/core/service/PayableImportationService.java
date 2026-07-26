package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.PayableImportationNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.PayableImportationServiceUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.UpdatePayableImportationStatusDto;
import com.totvs.payablemanagementapi.core.port.output.FileStoragePort;
import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEventPublisher;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayableImportationService implements PayableImportationServiceUseCase {

    private final FileStoragePort fileStoragePort;
    private final PayableImportPersistencePort payableImportPersistencePort;
    private final PayableImportEventPublisher payableImportEventPublisher;


    @Override
    public PayableImportation create(InputStream file) {
        PayableImportation payableImportation = PayableImportation.create();

        fileStoragePort.saveCsvFile(file);
        payableImportPersistencePort.save(payableImportation);
        payableImportEventPublisher.publish();

        return payableImportation;
    }

    @Override
    public PayableImportation updateStatus(
            Long id,
            UpdatePayableImportationStatusDto updateStatusDto
    ) {
        // Removar daqui e colocar no consumer
        if (id == null) {
            throw new InvalidPayableImportationException("O id da importação é obrigatório");
        }

        if (updateStatusDto == null || updateStatusDto.status() == null) {
            throw new InvalidPayableImportationException("O status da importação é obrigatório");
        }

        StatusPayableImportationEnum status =
                StatusPayableImportationEnum.fromCode(updateStatusDto.status());
        PayableImportation payableImportation = findById(id);
        payableImportation.updateStatus(status, updateStatusDto.errorMessage());

        return payableImportPersistencePort.save(payableImportation);
    }

    @Override
    public PayableImportation findById(Long id) {
        if (id == null) {
            throw new InvalidPayableImportationException("O id da importação é obrigatório");
        }

        PayableImportation payableImportation = payableImportPersistencePort.findById(id);
        if (payableImportation == null) {
            throw new PayableImportationNotFoundException(id);
        }

        return payableImportation;
    }

    @Override
    public List<PayableImportation> list() {
        return List.of();
    }

    @Override
    public List<PayableImportationItem> listPayableImportationItem(Long id) {
        return List.of();
    }
}
