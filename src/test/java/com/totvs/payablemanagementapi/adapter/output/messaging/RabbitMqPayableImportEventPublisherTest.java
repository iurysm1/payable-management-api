package com.totvs.payablemanagementapi.adapter.output.messaging;

import com.totvs.payablemanagementapi.core.exception.PayableImportEventPublishingException;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitMqPayableImportEventPublisherTest {

    @Test
    void shouldPublishEventWhenRabbitMqConfirmsIt() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        PayableImportRabbitMqProperties properties = new PayableImportRabbitMqProperties(
                "payable.importation",
                "payable.importation.requested",
                "payable.importation.requested",
                Duration.ofSeconds(10)
        );
        RabbitMqPayableImportEventPublisher publisher = new RabbitMqPayableImportEventPublisher(
                rabbitTemplate,
                properties
        );
        PayableImportEvent event = new PayableImportEvent(1L, "a1b2c3.csv");
        doAnswer(invocation -> {
            CorrelationData correlationData = invocation.getArgument(3);
            correlationData.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).convertAndSend(
                eq("payable.importation"),
                eq("payable.importation.requested"),
                eq(event),
                any(CorrelationData.class)
        );

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(
                eq("payable.importation"),
                eq("payable.importation.requested"),
                eq(event),
                any(CorrelationData.class)
        );
    }

    @Test
    void shouldThrowWhenRabbitMqRejectsPublication() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        PayableImportRabbitMqProperties properties = new PayableImportRabbitMqProperties(
                "payable.importation",
                "payable.importation.requested",
                "payable.importation.requested",
                Duration.ofSeconds(10)
        );
        RabbitMqPayableImportEventPublisher publisher = new RabbitMqPayableImportEventPublisher(
                rabbitTemplate,
                properties
        );
        PayableImportEvent event = new PayableImportEvent(1L, "a1b2c3.csv");
        doAnswer(invocation -> {
            CorrelationData correlationData = invocation.getArgument(3);
            correlationData.getFuture().complete(new CorrelationData.Confirm(false, "nack"));
            return null;
        }).when(rabbitTemplate).convertAndSend(
                eq("payable.importation"),
                eq("payable.importation.requested"),
                eq(event),
                any(CorrelationData.class)
        );

        assertThatThrownBy(() -> publisher.publish(event))
                .isInstanceOf(PayableImportEventPublishingException.class)
                .hasMessage("RabbitMQ rejeitou a publicação da mensagem: nack");
    }

    @Test
    void shouldThrowWhenRabbitMqReturnsUnroutableMessage() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        PayableImportRabbitMqProperties properties = properties(Duration.ofSeconds(10));
        RabbitMqPayableImportEventPublisher publisher = new RabbitMqPayableImportEventPublisher(
                rabbitTemplate,
                properties
        );
        PayableImportEvent event = new PayableImportEvent(1L, "a1b2c3.csv");
        doAnswer(invocation -> {
            CorrelationData correlationData = invocation.getArgument(3);
            correlationData.setReturned(new ReturnedMessage(
                    new Message(new byte[0], new MessageProperties()),
                    312,
                    "NO_ROUTE",
                    "payable.importation",
                    "payable.importation.requested"
            ));
            correlationData.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).convertAndSend(
                eq("payable.importation"),
                eq("payable.importation.requested"),
                eq(event),
                any(CorrelationData.class)
        );

        assertThatThrownBy(() -> publisher.publish(event))
                .isInstanceOf(PayableImportEventPublishingException.class)
                .hasMessage("RabbitMQ não encontrou destino para a mensagem: NO_ROUTE");
    }

    @Test
    void shouldThrowWhenRabbitMqDoesNotConfirmWithinTimeout() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitMqPayableImportEventPublisher publisher = new RabbitMqPayableImportEventPublisher(
                rabbitTemplate,
                properties(Duration.ZERO)
        );

        assertThatThrownBy(() -> publisher.publish(new PayableImportEvent(1L, "a1b2c3.csv")))
                .isInstanceOf(PayableImportEventPublishingException.class)
                .hasMessage("Timeout ao aguardar confirmação do RabbitMQ");
    }

    private PayableImportRabbitMqProperties properties(Duration confirmationTimeout) {
        return new PayableImportRabbitMqProperties(
                "payable.importation",
                "payable.importation.requested",
                "payable.importation.requested",
                confirmationTimeout
        );
    }
}
