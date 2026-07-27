package com.totvs.payablemanagementapi.adapter.input.messaging;

import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEvent;
import com.totvs.payablemanagementapi.core.port.input.PayableImportationProcessingUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayableImportRabbitMqListener {

    private final PayableImportationProcessingUseCase payableImportationProcessingUseCase;

    @RabbitListener(queues = "${app.rabbitmq.payable-importation.queue}")
    public void consume(PayableImportEvent event) {
        payableImportationProcessingUseCase.process(event);
        log.info(
                "Mensagem de importação recebida: payableImportationId={}, payableImportationCsvFilePath={}",
                event.payableImportationId(),
                event.payableImportationCsvFilePath()
        );
    }
}
