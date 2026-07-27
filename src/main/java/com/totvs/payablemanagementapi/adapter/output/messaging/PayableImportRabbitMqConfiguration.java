package com.totvs.payablemanagementapi.adapter.output.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PayableImportRabbitMqProperties.class)
public class PayableImportRabbitMqConfiguration {

    @Bean("payableImportExchange")
    DirectExchange payableImportExchange(PayableImportRabbitMqProperties properties) {
        return new DirectExchange(properties.exchange());
    }

    @Bean("payableImportQueue")
    Queue payableImportQueue(PayableImportRabbitMqProperties properties) {
        return QueueBuilder.durable(properties.queue()).build();
    }

    @Bean
    Binding payableImportBinding(
            @Qualifier("payableImportQueue") Queue payableImportQueue,
            @Qualifier("payableImportExchange") DirectExchange payableImportExchange,
            PayableImportRabbitMqProperties properties
    ) {
        return BindingBuilder.bind(payableImportQueue)
                .to(payableImportExchange)
                .with(properties.routingKey());
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
