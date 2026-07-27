package com.totvs.payablemanagementapi.adapter.output.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class PayableImportRabbitMqConfigurationTest {

    private final PayableImportRabbitMqConfiguration configuration =
            new PayableImportRabbitMqConfiguration();
    private final PayableImportRabbitMqProperties properties =
            new PayableImportRabbitMqProperties(
                    "payable.importation",
                    "payable.importation.requested",
                    "payable.importation.requested",
                    Duration.ofSeconds(10)
            );

    @Test
    void shouldDeclareMainQueueWithoutDeadLetterConfiguration() {
        Queue queue = configuration.payableImportQueue(properties);

        assertThat(queue.getName()).isEqualTo("payable.importation.requested");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.isAutoDelete()).isFalse();
        assertThat(queue.getArguments()).isEmpty();
    }

    @Test
    void shouldBindMainQueueToImportExchange() {
        Queue queue = configuration.payableImportQueue(properties);
        DirectExchange exchange = configuration.payableImportExchange(properties);

        Binding binding = configuration.payableImportBinding(queue, exchange, properties);

        assertThat(exchange.getName()).isEqualTo("payable.importation");
        assertThat(exchange.isDurable()).isTrue();
        assertThat(binding.getDestination()).isEqualTo("payable.importation.requested");
        assertThat(binding.getExchange()).isEqualTo("payable.importation");
        assertThat(binding.getRoutingKey()).isEqualTo("payable.importation.requested");
    }
}
