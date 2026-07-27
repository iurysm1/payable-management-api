package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.PayableImportationNotFoundException;
import com.totvs.payablemanagementapi.core.port.output.FileStoragePort;
import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEvent;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayableImportationProcessingServiceTest {

    @Mock
    private PayableImportPersistencePort payableImportPersistencePort;

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private PayableImportationProcessingService processingService;

    @Test
    void shouldValidateExistingImportationAndReadableCsvFileBeforeProcessing() {
        PayableImportEvent event = new PayableImportEvent(1L, "a1b2c3.csv");
        when(payableImportPersistencePort.findById(1L)).thenReturn(PayableImportation.create());
        when(fileStoragePort.getFile("a1b2c3.csv"))
                .thenReturn(new ByteArrayInputStream("header".getBytes()));

        processingService.process(event);

        verify(payableImportPersistencePort).findById(1L);
        verify(fileStoragePort).getFile("a1b2c3.csv");
    }

    @Test
    void shouldFailWhenImportationDoesNotExist() {
        PayableImportEvent event = new PayableImportEvent(99L, "a1b2c3.csv");
        when(payableImportPersistencePort.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> processingService.process(event))
                .isInstanceOf(PayableImportationNotFoundException.class);
    }
}
