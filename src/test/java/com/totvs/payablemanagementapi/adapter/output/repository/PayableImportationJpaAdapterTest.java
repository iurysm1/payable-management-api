package com.totvs.payablemanagementapi.adapter.output.repository;

import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayableImportationJpaAdapterTest {

    @Mock
    private PayableImportationRepository payableImportationRepository;

    @Mock
    private PayableImportationItemRepository payableImportationItemRepository;

    @InjectMocks
    private PayableImportationJpaAdapter adapter;

    @Test
    void shouldDelegateSaveFindByIdAndFindAllToRepository() {
        PayableImportation importation = PayableImportation.create();
        List<PayableImportation> importations = List.of(importation);

        when(payableImportationRepository.save(importation)).thenReturn(importation);
        when(payableImportationRepository.findById(1L)).thenReturn(Optional.of(importation));
        when(payableImportationRepository.findAll()).thenReturn(importations);

        assertThat(adapter.save(importation)).isSameAs(importation);
        assertThat(adapter.findById(1L)).isSameAs(importation);
        assertThat(adapter.findAll()).isSameAs(importations);

        verify(payableImportationRepository).save(importation);
        verify(payableImportationRepository).findById(1L);
        verify(payableImportationRepository).findAll();
    }

    @Test
    void shouldReturnNullWhenImportationDoesNotExist() {
        when(payableImportationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isNull();

        verify(payableImportationRepository).findById(99L);
    }

    @Test
    void shouldFindItemsByImportationId() {
        List<PayableImportationItem> items = List.of(PayableImportationItem.create(1L));

        when(payableImportationItemRepository.findByPayableImportationId(1L)).thenReturn(items);

        assertThat(adapter.findByPayableImportationId(1L)).isSameAs(items);

        verify(payableImportationItemRepository).findByPayableImportationId(1L);
    }
}
