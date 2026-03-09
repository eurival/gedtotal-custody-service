package br.com.arquivototal.gedtotalcustody.application.service;

import br.com.arquivototal.gedtotalcustody.domain.event.CustodyCommandEvent;
import br.com.arquivototal.gedtotalcustody.domain.event.CustodyFailureEvent;
import br.com.arquivototal.gedtotalcustody.domain.event.CustodyResultEvent;
import br.com.arquivototal.gedtotalcustody.domain.enumeration.CustodyStepType;
import br.com.arquivototal.gedtotalcustody.domain.enumeration.ProcessingStatus;
import br.com.arquivototal.gedtotalcustody.infrastructure.http.CustodyDocumentPayload;
import br.com.arquivototal.gedtotalcustody.infrastructure.http.GedtotalApiClient;
import br.com.arquivototal.gedtotalcustody.infrastructure.kafka.CustodyEventPublisher;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustodyJobService {

    private final GedtotalApiClient gedtotalApiClient;
    private final CustodyEventPublisher custodyEventPublisher;

    public void handle(CustodyCommandEvent event) {
        log.info(
            "Recebido comando de custodia jobId={} arquivoId={} custodiaDocumentoId={}",
            event.jobId(),
            event.arquivoId(),
            event.custodiaDocumentoId()
        );

        try {
            String payloadUrl = "/api/internal/custodia/documentos/%d/payload".formatted(event.arquivoId());
            CustodyDocumentPayload payload = gedtotalApiClient.fetchPayload(payloadUrl);

            log.info(
                "Payload de custodia obtido jobId={} arquivoId={} formularioId={} nomeArquivo={}",
                event.jobId(),
                payload.arquivoId(),
                payload.formularioId(),
                payload.nomeArquivo()
            );

            custodyEventPublisher.publishResult(
                new CustodyResultEvent(
                    event.jobId(),
                    event.custodiaDocumentoId(),
                    event.arquivoId(),
                    event.masterDadosIndexacaoId(),
                    event.tenantRootId(),
                    event.clienteId(),
                    event.clientePaiId(),
                    event.departamentoId(),
                    event.projetoId(),
                    event.formularioId(),
                    ProcessingStatus.RECEBIDO,
                    event.hashFinalDocumento(),
                    null,
                    null,
                    null,
                    Map.of("message", "Etapa de custodia recebida e pronta para implementacao", "nomeArquivo", payload.nomeArquivo()),
                    event.traceId()
                )
            );
        } catch (Exception ex) {
            log.error(
                "Falha ao processar custodia jobId={} arquivoId={} erro={}",
                event.jobId(),
                event.arquivoId(),
                ex.getMessage(),
                ex
            );
            custodyEventPublisher.publishFailure(
                new CustodyFailureEvent(
                    event.jobId(),
                    event.custodiaDocumentoId(),
                    event.arquivoId(),
                    event.masterDadosIndexacaoId(),
                    event.tenantRootId(),
                    event.clienteId(),
                    event.clientePaiId(),
                    event.departamentoId(),
                    event.projetoId(),
                    event.formularioId(),
                    CustodyStepType.HASH_FINAL,
                    "CUSTODY_JOB_ERROR",
                    ex.getMessage(),
                    event.traceId()
                )
            );
        }
    }
}
