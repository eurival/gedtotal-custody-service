package br.com.arquivototal.gedtotalcustody.infrastructure.kafka;

import br.com.arquivototal.gedtotalcustody.application.service.CustodyJobService;
import br.com.arquivototal.gedtotalcustody.domain.event.CustodyCommandEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustodyCommandListener {

    private final ObjectMapper objectMapper;
    private final CustodyJobService custodyJobService;

    @KafkaListener(topics = "${app.kafka.topics.custody-request}", groupId = "${spring.application.name}")
    public void onMessage(String payload) throws Exception {
        CustodyCommandEvent event = objectMapper.readValue(payload, CustodyCommandEvent.class);
        log.info("Mensagem de custodia recebida jobId={} arquivoId={}", event.jobId(), event.arquivoId());
        custodyJobService.handle(event);
    }
}
