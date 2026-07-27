package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayableImportationLineServiceTest {

    @Mock
    private PayableUseCase payableUseCase;

    @Mock
    private PayableImportPersistencePort payableImportPersistencePort;

    @InjectMocks
    private PayableImportationLineService payableImportationLineService;

    @Test
    void shouldSavePayableBeforeSuccessfulImportationItem() {
        PayableDto payableDto = payableDto();
        Payable payable = Payable.builder().id(20L).build();
        when(payableUseCase.save(payableDto)).thenReturn(payable);

        payableImportationLineService.process(1L, payableDto);

        ArgumentCaptor<PayableImportationItem> itemCaptor =
                ArgumentCaptor.forClass(PayableImportationItem.class);
        InOrder persistenceOrder = inOrder(payableUseCase, payableImportPersistencePort);
        persistenceOrder.verify(payableUseCase).save(payableDto);
        persistenceOrder.verify(payableImportPersistencePort).saveItem(itemCaptor.capture());

        assertThat(itemCaptor.getValue().getPayableImportationId()).isEqualTo(1L);
        assertThat(itemCaptor.getValue().getPayableId()).isEqualTo(20L);
    }

    @Test
    void shouldNotSaveImportationItemWhenPayableFails() {
        PayableDto payableDto = payableDto();
        InvalidPayableException exception = new InvalidPayableException("Conta inválida");
        when(payableUseCase.save(payableDto)).thenThrow(exception);

        assertThatThrownBy(() -> payableImportationLineService.process(1L, payableDto))
                .isSameAs(exception);

        verifyNoInteractions(payableImportPersistencePort);
    }

    @Test
    void shouldPropagateItemPersistenceFailure() {
        PayableDto payableDto = payableDto();
        Payable payable = Payable.builder().id(20L).build();
        RuntimeException exception = new RuntimeException("Falha ao salvar item");
        when(payableUseCase.save(payableDto)).thenReturn(payable);
        doThrow(exception).when(payableImportPersistencePort)
                .saveItem(org.mockito.ArgumentMatchers.any(PayableImportationItem.class));

        assertThatThrownBy(() -> payableImportationLineService.process(1L, payableDto))
                .isSameAs(exception);

        verify(payableUseCase).save(payableDto);
        verify(payableImportPersistencePort).saveItem(
                org.mockito.ArgumentMatchers.any(PayableImportationItem.class)
        );
    }

    private PayableDto payableDto() {
        return new PayableDto(
                null,
                "Aluguel",
                new BigDecimal("100.00"),
                StatusPayableEnum.PENDENTE,
                null,
                null,
                2L
        );
    }
}
