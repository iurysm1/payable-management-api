package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.FileStorageException;
import com.totvs.payablemanagementapi.core.port.input.PayableImportationServiceUseCase;
import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.core.port.input.dto.UpdatePayableImportationStatusDto;
import com.totvs.payablemanagementapi.core.port.output.FileStoragePort;
import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEvent;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationItemEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayableImportationProcessingServiceTest {

    @Mock
    private PayableImportationServiceUseCase payableImportationServiceUseCase;

    @Mock
    private PayableUseCase payableUseCase;

    @Mock
    private PayableImportPersistencePort payableImportPersistencePort;

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private PayableImportationProcessingService processingService;

    @Test
    void shouldCreatePayablesAndSuccessfulItemsAndCompleteImportation() {
        PayableImportEvent event = new PayableImportEvent(1L, "a1b2c3.csv");
        PayableDto payableDto = new PayableDto(
                null,
                "Pagamento de aluguel",
                new BigDecimal("15012.00"),
                StatusPayableEnum.PAGO,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 1, 11),
                2L
        );
        when(payableImportationServiceUseCase.findById(1L)).thenReturn(PayableImportation.create());
        when(fileStoragePort.getFile("a1b2c3.csv")).thenReturn(csv(
                "description,amount,status,expirationDate,paymentDate,supplierId\n"
                        + "Pagamento de aluguel,15012.00,PAGO,2026-08-10,2026-01-11,2\n"
        ));
        when(payableUseCase.save(payableDto)).thenReturn(Payable.builder().id(20L).build());

        processingService.process(event);

        ArgumentCaptor<UpdatePayableImportationStatusDto> statusCaptor =
                ArgumentCaptor.forClass(UpdatePayableImportationStatusDto.class);
        ArgumentCaptor<PayableImportationItem> itemCaptor =
                ArgumentCaptor.forClass(PayableImportationItem.class);
        verify(payableImportationServiceUseCase, times(2)).updateStatus(eq(1L), statusCaptor.capture());
        verify(payableUseCase).save(payableDto);
        verify(payableImportPersistencePort).saveItem(itemCaptor.capture());

        assertThat(statusCaptor.getAllValues())
                .extracting(UpdatePayableImportationStatusDto::status)
                .containsExactly(
                        StatusPayableImportationEnum.PROCESSING.getCode(),
                        StatusPayableImportationEnum.COMPLETED.getCode()
                );
        assertThat(itemCaptor.getValue().getStatus()).isEqualTo(StatusPayableImportationItemEnum.SUCCESS);
        assertThat(itemCaptor.getValue().getPayableId()).isEqualTo(20L);
    }

    @Test
    void shouldPersistErrorItemAndContinueProcessingRemainingLines() {
        PayableImportEvent event = new PayableImportEvent(1L, "a1b2c3.csv");
        PayableDto invalidDto = new PayableDto(
                null,
                "Conta inválida",
                new BigDecimal("10.00"),
                StatusPayableEnum.PENDENTE,
                null,
                null,
                2L
        );
        PayableDto validDto = new PayableDto(
                null,
                "Conta válida",
                new BigDecimal("20.00"),
                StatusPayableEnum.PENDENTE,
                null,
                null,
                2L
        );
        when(payableImportationServiceUseCase.findById(1L)).thenReturn(PayableImportation.create());
        when(fileStoragePort.getFile("a1b2c3.csv")).thenReturn(csv(
                "description,amount,status,expirationDate,paymentDate,supplierId\n"
                        + "Conta inválida,10.00,PENDENTE,,,2\n"
                        + "Conta válida,20.00,PENDENTE,,,2\n"
        ));
        doThrow(new InvalidPayableException("Fornecedor inválido"))
                .when(payableUseCase).save(invalidDto);
        when(payableUseCase.save(validDto)).thenReturn(Payable.builder().id(21L).build());

        processingService.process(event);

        ArgumentCaptor<UpdatePayableImportationStatusDto> statusCaptor =
                ArgumentCaptor.forClass(UpdatePayableImportationStatusDto.class);
        ArgumentCaptor<PayableImportationItem> itemCaptor =
                ArgumentCaptor.forClass(PayableImportationItem.class);
        verify(payableImportationServiceUseCase, times(2)).updateStatus(eq(1L), statusCaptor.capture());
        verify(payableUseCase).save(invalidDto);
        verify(payableUseCase).save(validDto);
        verify(payableImportPersistencePort, times(2)).saveItem(itemCaptor.capture());

        assertThat(statusCaptor.getAllValues())
                .extracting(UpdatePayableImportationStatusDto::status)
                .containsExactly(
                        StatusPayableImportationEnum.PROCESSING.getCode(),
                        StatusPayableImportationEnum.COMPLETED_WITH_ERRORS.getCode()
                );
        assertThat(itemCaptor.getAllValues())
                .extracting(PayableImportationItem::getStatus)
                .containsExactly(
                        StatusPayableImportationItemEnum.ERROR,
                        StatusPayableImportationItemEnum.SUCCESS
                );
        assertThat(itemCaptor.getAllValues().getFirst().getErrorMessage()).isEqualTo("Fornecedor inválido");
    }

    @Test
    void shouldMarkImportationAsFailedWhenFileCannotBeRead() {
        PayableImportEvent event = new PayableImportEvent(1L, "a1b2c3.csv");
        when(payableImportationServiceUseCase.findById(1L)).thenReturn(PayableImportation.create());
        when(fileStoragePort.getFile("a1b2c3.csv"))
                .thenThrow(new FileStorageException("Arquivo indisponível"));

        processingService.process(event);

        ArgumentCaptor<UpdatePayableImportationStatusDto> statusCaptor =
                ArgumentCaptor.forClass(UpdatePayableImportationStatusDto.class);
        verify(payableImportationServiceUseCase, times(2)).updateStatus(eq(1L), statusCaptor.capture());

        assertThat(statusCaptor.getAllValues())
                .extracting(UpdatePayableImportationStatusDto::status)
                .containsExactly(
                        StatusPayableImportationEnum.PROCESSING.getCode(),
                        StatusPayableImportationEnum.FAILED.getCode()
                );
        assertThat(statusCaptor.getAllValues().getLast().errorMessage()).isEqualTo("Arquivo indisponível");
    }

    @Test
    void shouldIgnoreImportationThatIsNotPending() {
        PayableImportEvent event = new PayableImportEvent(1L, "a1b2c3.csv");
        PayableImportation completedImportation = PayableImportation.builder()
                .status(StatusPayableImportationEnum.COMPLETED)
                .build();
        when(payableImportationServiceUseCase.findById(1L)).thenReturn(completedImportation);

        processingService.process(event);

        verify(payableImportationServiceUseCase, never()).updateStatus(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(UpdatePayableImportationStatusDto.class)
        );
        verifyNoInteractions(payableUseCase, payableImportPersistencePort, fileStoragePort);
    }

    private ByteArrayInputStream csv(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}
