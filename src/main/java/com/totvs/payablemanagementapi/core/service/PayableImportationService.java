package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.PayableImportationNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.PayableImportationServiceUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.UpdatePayableImportationStatusDto;
import com.totvs.payablemanagementapi.core.port.output.FileStoragePort;
import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEvent;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEventPublisher;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayableImportationService implements PayableImportationServiceUseCase {

    private final FileStoragePort fileStoragePort;
    private final PayableImportPersistencePort payableImportPersistencePort;
    private final PayableImportEventPublisher payableImportEventPublisher;


    @Override
    public PayableImportation create(InputStream file) {
        PayableImportation payableImportation = PayableImportation.create();

        String csvFilePath = fileStoragePort.saveCsvFile(file);
        PayableImportation savedImportation = null;

        try {
            savedImportation = payableImportPersistencePort.save(payableImportation);
            payableImportEventPublisher.publish(new PayableImportEvent(
                    savedImportation.getId(),
                    csvFilePath
            ));

            return savedImportation;
        } catch (RuntimeException exception) {
            log.error("Falha ao criar ou publicar a importação", exception);
            compensateCreation(savedImportation, csvFilePath);
            throw exception;
        }
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

        PayableImportation updatedImportation = payableImportPersistencePort.save(payableImportation);
        log.info("Status da importação {} atualizado para {}", id, status);
        return updatedImportation;
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
        return payableImportPersistencePort.findAll();
    }

    @Override
    public List<PayableImportationItem> listPayableImportationItem(Long id) {
        findById(id);
        return payableImportPersistencePort.findByPayableImportationId(id);
    }

    private void compensateCreation(PayableImportation savedImportation, String csvFilePath) {
        if (savedImportation != null && savedImportation.getId() != null) {
            try {
                payableImportPersistencePort.deleteById(savedImportation.getId());
            } catch (RuntimeException exception) {
                log.error("Falha ao remover a importação {} durante a compensação",
                        savedImportation.getId(), exception);
            }
        }

        try {
            fileStoragePort.deleteFile(csvFilePath);
        } catch (RuntimeException exception) {
            log.error("Falha ao remover o arquivo {} durante a compensação", csvFilePath, exception);
        }
    }
}
