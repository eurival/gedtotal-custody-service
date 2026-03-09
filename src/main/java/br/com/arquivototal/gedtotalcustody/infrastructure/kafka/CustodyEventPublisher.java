package br.com.arquivototal.gedtotalcustody.infrastructure.kafka;

import br.com.arquivototal.gedtotalcustody.config.KafkaTopicsProperties;
import br.com.arquivototal.gedtotalcustody.domain.event.CustodyFailureEvent;
import br.com.arquivototal.gedtotalcustody.domain.event.CustodyResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustodyEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    public void publishResult(CustodyResultEvent event) {
        log.info("Publicando resultado de custodia jobId={} topico={}", event.jobId(), kafkaTopicsProperties.custodyResult());
        kafkaTemplate.send(kafkaTopicsProperties.custodyResult(), event.jobId(), event);
    }

    public void publishFailure(CustodyFailureEvent event) {
        log.info("Publicando falha de custodia jobId={} topico={}", event.jobId(), kafkaTopicsProperties.custodyFailure());
        kafkaTemplate.send(kafkaTopicsProperties.custodyFailure(), event.jobId(), event);
    }
}
