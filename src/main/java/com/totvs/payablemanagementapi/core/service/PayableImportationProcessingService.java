package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.PayableImportationNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.PayableImportationProcessingUseCase;
import com.totvs.payablemanagementapi.core.port.output.FileStoragePort;
import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEvent;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class PayableImportationProcessingService implements PayableImportationProcessingUseCase {

    private final PayableImportPersistencePort payableImportPersistencePort;
    private final FileStoragePort fileStoragePort;

    @Override
    public void process(PayableImportEvent event) {
        validateForProcessing(event);
        // O processamento do CSV será implementado em um caso de uso posterior.
    }

    private void validateForProcessing(PayableImportEvent event) {
        if (event == null || event.payableImportationId() == null) {
            throw new InvalidPayableImportationException("O id da importação é obrigatório");
        }

        if (event.payableImportationCsvFilePath() == null
                || event.payableImportationCsvFilePath().isBlank()) {
            throw new InvalidPayableImportationException("O caminho do arquivo CSV é obrigatório");
        }

        if (payableImportPersistencePort.findById(event.payableImportationId()) == null) {
            throw new PayableImportationNotFoundException(event.payableImportationId());
        }

        try (InputStream ignored = fileStoragePort.getFile(event.payableImportationCsvFilePath())) {
            // Abrir o arquivo garante que ele está disponível antes do processamento.
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível validar o arquivo CSV da importação", exception);
        }
    }
}
