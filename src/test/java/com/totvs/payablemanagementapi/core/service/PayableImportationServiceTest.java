package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.PayableImportEventPublishingException;
import com.totvs.payablemanagementapi.core.exception.PayableImportationNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.dto.UpdatePayableImportationStatusDto;
import com.totvs.payablemanagementapi.core.port.output.FileStoragePort;
import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEvent;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEventPublisher;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayableImportationServiceTest {

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private PayableImportPersistencePort payableImportPersistencePort;

    @Mock
    private PayableImportEventPublisher payableImportEventPublisher;

    @InjectMocks
    private PayableImportationService payableImportationService;

    @Test
    void shouldPublishImportationEventAfterSavingFileAndImportation() {
        ByteArrayInputStream file = new ByteArrayInputStream("header".getBytes());
        PayableImportation savedImportation = PayableImportation.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(StatusPayableImportationEnum.PENDING)
                .build();
        when(fileStoragePort.saveCsvFile(file)).thenReturn("a1b2c3.csv");
        when(payableImportPersistencePort.save(org.mockito.ArgumentMatchers.any(PayableImportation.class)))
                .thenReturn(savedImportation);

        payableImportationService.create(file);

        verify(payableImportEventPublisher).publish(
                new PayableImportEvent(1L, "a1b2c3.csv")
        );
    }

    @Test
    void shouldCompensateFileAndImportationWhenPublicationFails() {
        ByteArrayInputStream file = new ByteArrayInputStream("header".getBytes());
        PayableImportation savedImportation = PayableImportation.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(StatusPayableImportationEnum.PENDING)
                .build();
        when(fileStoragePort.saveCsvFile(file)).thenReturn("a1b2c3.csv");
        when(payableImportPersistencePort.save(org.mockito.ArgumentMatchers.any(PayableImportation.class)))
                .thenReturn(savedImportation);
        doThrow(new PayableImportEventPublishingException("Timeout ao aguardar confirmação do RabbitMQ"))
                .when(payableImportEventPublisher)
                .publish(new PayableImportEvent(1L, "a1b2c3.csv"));

        assertThatThrownBy(() -> payableImportationService.create(file))
                .isInstanceOf(PayableImportEventPublishingException.class);

        verify(payableImportPersistencePort).deleteById(1L);
        verify(fileStoragePort).deleteFile("a1b2c3.csv");
    }

    @Test
    void shouldUpdateImportationStatus() {
        PayableImportation importation = PayableImportation.create();
        when(payableImportPersistencePort.findById(1L)).thenReturn(importation);
        when(payableImportPersistencePort.save(importation)).thenReturn(importation);

        PayableImportation result = payableImportationService.updateStatus(
                1L,
                new UpdatePayableImportationStatusDto(
                        StatusPayableImportationEnum.PROCESSING.getCode(),
                        null
                )
        );

        assertThat(result.getStatus()).isEqualTo(StatusPayableImportationEnum.PROCESSING);
        verify(payableImportPersistencePort).save(importation);
    }

    @Test
    void shouldRejectNullStatus() {
        assertThatThrownBy(() -> payableImportationService.updateStatus(
                1L, new UpdatePayableImportationStatusDto(null, null)
        ))
                .isInstanceOf(InvalidPayableImportationException.class)
                .hasMessage("O status da importação é obrigatório");
    }

    @Test
    void shouldThrowWhenImportationDoesNotExist() {
        when(payableImportPersistencePort.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> payableImportationService.updateStatus(
                99L,
                new UpdatePayableImportationStatusDto(
                        StatusPayableImportationEnum.PROCESSING.getCode(),
                        null
                )
        ))
                .isInstanceOf(PayableImportationNotFoundException.class)
                .hasMessage("Importação de contas a pagar com id 99 não encontrada");
    }
}
