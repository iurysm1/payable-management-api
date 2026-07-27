package com.totvs.payablemanagementapi.adapter.input.messaging;

import com.totvs.payablemanagementapi.core.port.input.PayableImportationProcessingUseCase;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PayableImportRabbitMqListenerTest {

    @Test
    void shouldValidateEventBeforeProcessingIt() {
        PayableImportationProcessingUseCase useCase = mock(PayableImportationProcessingUseCase.class);
        PayableImportRabbitMqListener listener = new PayableImportRabbitMqListener(useCase);
        PayableImportEvent event = new PayableImportEvent(1L, "a1b2c3.csv");

        assertThatCode(() -> listener.consume(event))
                .doesNotThrowAnyException();

        verify(useCase).process(event);
    }
}
