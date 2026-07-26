package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.PayableImportationNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.dto.UpdatePayableImportationStatusDto;
import com.totvs.payablemanagementapi.core.port.output.FileStoragePort;
import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEventPublisher;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
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
