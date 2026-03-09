package br.com.arquivototal.gedtotalcustody.application.service;

import br.com.arquivototal.gedtotalcustody.domain.event.CustodyCommandEvent;
import br.com.arquivototal.gedtotalcustody.domain.event.CustodyFailureEvent;
import br.com.arquivototal.gedtotalcustody.domain.event.CustodyResultEvent;
import br.com.arquivototal.gedtotalcustody.domain.enumeration.CustodyStepType;
import br.com.arquivototal.gedtotalcustody.domain.enumeration.ProcessingStatus;
import br.com.arquivototal.gedtotalcustody.infrastructure.http.CustodyDocumentPayload;
import br.com.arquivototal.gedtotalcustody.infrastructure.http.GedtotalApiClient;
import br.com.arquivototal.gedtotalcustody.infrastructure.kafka.CustodyEventPublisher;
import java.security.MessageDigest;
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
            CustodyDocumentPayload payload = gedtotalApiClient.fetchPayload(
                "/api/internal/custodia/documentos/%d/payload".formatted(event.arquivoId())
            );
            byte[] content = gedtotalApiClient.fetchDocumentContent(payload.downloadUrl());
            String hashCalculado = sha256Hex(content);

            log.info(
                "Payload de custodia obtido jobId={} arquivoId={} formularioId={} nomeArquivo={} bytes={}",
                event.jobId(),
                payload.arquivoId(),
                payload.formularioId(),
                payload.nomeArquivo(),
                content.length
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
                    ProcessingStatus.CONCLUIDO,
                    hashCalculado,
                    null,
                    null,
                    null,
                    Map.of(
                        "message",
                        "Etapa de custodia processada pelo worker",
                        "nomeArquivo",
                        payload.nomeArquivo(),
                        "bytes",
                        content.length,
                        "hashPayload",
                        payload.hashAtual(),
                        "hashComando",
                        event.hashFinalDocumento()
                    ),
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

    private String sha256Hex(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Nao foi possivel calcular SHA-256", ex);
        }
    }
}
