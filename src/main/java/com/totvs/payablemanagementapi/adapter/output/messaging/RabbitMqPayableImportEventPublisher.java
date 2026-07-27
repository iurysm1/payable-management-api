package com.totvs.payablemanagementapi.adapter.output.messaging;

import com.totvs.payablemanagementapi.core.exception.PayableImportEventPublishingException;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEvent;
import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class RabbitMqPayableImportEventPublisher implements PayableImportEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final PayableImportRabbitMqProperties properties;

    @Override
    public void publish(PayableImportEvent event) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        try {
            rabbitTemplate.convertAndSend(
                    properties.exchange(),
                    properties.routingKey(),
                    event,
                    correlationData
            );

            CorrelationData.Confirm confirm = correlationData.getFuture().get(
                    properties.confirmationTimeout().toMillis(),
                    TimeUnit.MILLISECONDS
            );

            if (!confirm.isAck()) {
                throw new PayableImportEventPublishingException(
                        "RabbitMQ rejeitou a publicação da mensagem: " + confirm.getReason()
                );
            }

            if (correlationData.getReturned() != null) {
                throw new PayableImportEventPublishingException(
                        "RabbitMQ não encontrou destino para a mensagem: "
                                + correlationData.getReturned().getReplyText()
                );
            }
        } catch (TimeoutException exception) {
            throw new PayableImportEventPublishingException(
                    "Timeout ao aguardar confirmação do RabbitMQ", exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PayableImportEventPublishingException(
                    "Publicação no RabbitMQ foi interrompida", exception
            );
        } catch (ExecutionException exception) {
            throw new PayableImportEventPublishingException(
                    "Falha ao aguardar confirmação do RabbitMQ", exception
            );
        } catch (PayableImportEventPublishingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new PayableImportEventPublishingException(
                    "Falha ao publicar mensagem no RabbitMQ", exception
            );
        }
    }
}
