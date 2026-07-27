package com.totvs.payablemanagementapi.adapter.output.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.rabbitmq.payable-importation")
public record PayableImportRabbitMqProperties(
        String exchange,
        String queue,
        String routingKey,
        String deadLetterExchange,
        String deadLetterQueue,
        Duration confirmationTimeout
) {
}
