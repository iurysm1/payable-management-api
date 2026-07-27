package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.port.input.PayableImportationLineUseCase;
import com.totvs.payablemanagementapi.core.port.input.PayableImportationProcessingUseCase;
import com.totvs.payablemanagementapi.core.port.input.PayableImportationServiceUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.core.port.input.dto.UpdatePayableImportationStatusDto;
import com.totvs.payablemanagementapi.core.port.output.FileStoragePort;
import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEvent;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayableImportationProcessingService implements PayableImportationProcessingUseCase {

    private final PayableImportationServiceUseCase payableImportationServiceUseCase;
    private final PayableImportationLineUseCase payableImportationLineUseCase;
    private final PayableImportPersistencePort payableImportPersistencePort;
    private final FileStoragePort fileStoragePort;

    @Override
    public void process(PayableImportEvent event) {
        Long payableImportationId = event == null ? null : event.payableImportationId();

        try {
            if (!isPendingImportation(event)) {
                return;
            }

            updateStatus(payableImportationId, StatusPayableImportationEnum.PROCESSING, null);
            boolean hasErrors = processCsv(event);

            updateStatus(
                    payableImportationId,
                    hasErrors
                            ? StatusPayableImportationEnum.COMPLETED_WITH_ERRORS
                            : StatusPayableImportationEnum.COMPLETED,
                    null
            );
        } catch (Exception exception) {
            log.error("Falha ao processar a importação {}", payableImportationId, exception);
            updateAsFailed(payableImportationId, exception);
        } finally {
            deleteCsvFile(event);
        }
    }

    private boolean isPendingImportation(PayableImportEvent event) {
        if (event == null) {
            throw new InvalidPayableImportationException("Payload não pode ser ausente");
        }

        if (event.payableImportationCsvFilePath() == null
                || event.payableImportationCsvFilePath().isBlank()) {
            throw new InvalidPayableImportationException("O caminho do arquivo CSV é obrigatório");
        }

        PayableImportation payableImportation =
                payableImportationServiceUseCase.findById(event.payableImportationId());
        if (payableImportation.getStatus() != StatusPayableImportationEnum.PENDING) {
            log.warn(
                    "Importação {} ignorada: status atual {} não permite processamento",
                    event.payableImportationId(),
                    payableImportation.getStatus()
            );
            return false;
        }

        return true;
    }

    private boolean processCsv(PayableImportEvent event) throws IOException {
        boolean hasErrors = false;

        try (InputStream inputStream = fileStoragePort.getFile(event.payableImportationCsvFilePath());
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            validateHeader(reader.readLine());

            String line;
            long lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                if (!processLine(event.payableImportationId(), line, lineNumber)) {
                    hasErrors = true;
                }
            }
        }

        return hasErrors;
    }

    private boolean processLine(Long payableImportationId, String line, long lineNumber) {
        try {
            payableImportationLineUseCase.process(payableImportationId, toPayableDto(line));
            return true;
        } catch (Exception exception) {
            log.warn(
                    "Falha ao processar a linha {} da importação {}: {}",
                    lineNumber,
                    payableImportationId,
                    resolveErrorMessage(exception)
            );
            payableImportPersistencePort.saveItem(
                    PayableImportationItem.createError(payableImportationId, resolveErrorMessage(exception))
            );
            return false;
        }
    }

    private PayableDto toPayableDto(String line) {
        List<String> values = parseCsvLine(line);
        if (values.size() != 6) {
            throw new IllegalArgumentException("A linha do CSV deve conter 6 colunas");
        }

        String description = values.get(0);
        BigDecimal amount = new BigDecimal(values.get(1));
        StatusPayableEnum status = parseStatus(values.get(2));
        LocalDate expirationDate = parseDate(values.get(3));
        LocalDate paymentDate = parseDate(values.get(4));
        Long supplierId = parseLong(values.get(5));

        return new PayableDto(
                null,
                description,
                amount,
                status,
                expirationDate,
                paymentDate,
                supplierId
        );
    }

    private void validateHeader(String header) {
        if (header == null || !parseCsvLine(header).equals(List.of(
                "description",
                "amount",
                "status",
                "expirationDate",
                "paymentDate",
                "supplierId"
        ))) {
            throw new IllegalArgumentException(
                    "Cabeçalho CSV inválido. Esperado: description,amount,status,expirationDate,paymentDate,supplierId"
            );
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append(current);
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                values.add(value.toString().trim());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }

        if (quoted) {
            throw new IllegalArgumentException("Campo CSV com aspas não finalizadas");
        }

        values.add(value.toString().trim());
        return values;
    }

    private StatusPayableEnum parseStatus(String value) {
        return value.isBlank() ? null : StatusPayableEnum.fromName(value);
    }

    private LocalDate parseDate(String value) {
        return value.isBlank() ? null : LocalDate.parse(value);
    }

    private Long parseLong(String value) {
        return value.isBlank() ? null : Long.valueOf(value);
    }

    private void updateStatus(
            Long payableImportationId,
            StatusPayableImportationEnum status,
            String errorMessage
    ) {
        payableImportationServiceUseCase.updateStatus(
                payableImportationId,
                new UpdatePayableImportationStatusDto(status.getCode(), errorMessage)
        );
    }

    private void updateAsFailed(Long payableImportationId, Exception exception) {
        if (payableImportationId == null) {
            return;
        }

        try {
            updateStatus(
                    payableImportationId,
                    StatusPayableImportationEnum.FAILED,
                    resolveErrorMessage(exception)
            );
        } catch (Exception statusException) {
            log.error("Falha ao atualizar a importação {} para FAILED", payableImportationId, statusException);
        }
    }

    private void deleteCsvFile(PayableImportEvent event) {
        if (event == null
                || event.payableImportationCsvFilePath() == null
                || event.payableImportationCsvFilePath().isBlank()) {
            return;
        }

        try {
            fileStoragePort.deleteFile(event.payableImportationCsvFilePath());
        } catch (RuntimeException exception) {
            log.error(
                    "Falha ao remover o arquivo {} após o processamento da importação {}",
                    event.payableImportationCsvFilePath(),
                    event.payableImportationId(),
                    exception
            );
        }
    }

    private String resolveErrorMessage(Exception exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Falha inesperada ao processar a importação"
                : exception.getMessage();
    }
}
