package br.com.arquivototal.gedtotalcustody.application.service;

import br.com.arquivototal.gedtotalcustody.domain.event.CustodyCommandEvent;
import br.com.arquivototal.gedtotalcustody.domain.event.CustodyFailureEvent;
import br.com.arquivototal.gedtotalcustody.domain.event.CustodyResultEvent;
import br.com.arquivototal.gedtotalcustody.domain.enumeration.CustodyStepType;
import br.com.arquivototal.gedtotalcustody.domain.enumeration.ProcessingStatus;
import br.com.arquivototal.gedtotalcustody.application.service.support.HashUtils;
import br.com.arquivototal.gedtotalcustody.infrastructure.http.CustodyDocumentPayload;
import br.com.arquivototal.gedtotalcustody.infrastructure.http.GedtotalApiClient;
import br.com.arquivototal.gedtotalcustody.infrastructure.kafka.CustodyEventPublisher;
import java.util.List;
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
    private final MerkleTreeService merkleTreeService;
    private final BlockchainAnchorService blockchainAnchorService;

    public void handle(CustodyCommandEvent event) {
        log.info(
            "Recebido comando de custodia jobId={} arquivoId={} custodiaDocumentoId={}",
            event.jobId(),
            event.arquivoId(),
            event.custodiaDocumentoId()
        );

        try {
            CustodyDocumentPayload payload = gedtotalApiClient.fetchPayload(
                event.sourceArquivoId() != null
                    ? "/api/internal/custodia/documentos/%d/payload?sourceArquivoId=%d".formatted(event.arquivoId(), event.sourceArquivoId())
                    : "/api/internal/custodia/documentos/%d/payload".formatted(event.arquivoId())
            );
            byte[] content = gedtotalApiClient.fetchDocumentContent(payload.downloadUrl());
            String hashCalculado = HashUtils.sha256Hex(content);
            String leafHash = event.hashFinalDocumento() != null && !event.hashFinalDocumento().isBlank() ? event.hashFinalDocumento() : hashCalculado;
            String rootHash = merkleTreeService.buildRoot(List.of(leafHash));
            AnchorResult anchorResult = blockchainAnchorService.anchor(rootHash);

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
                    rootHash,
                    anchorResult.txHash(),
                    anchorResult.network(),
                    anchorResult.contractAddress(),
                    Map.of(
                        "message",
                        "Etapa de custodia processada pelo worker",
                        "nomeArquivo",
                        payload.nomeArquivo(),
                        "bytes",
                        content.length,
                        "hashPayload",
                        payload.hashAtual(),
                        "leafHash",
                        leafHash,
                        "hashMaterialCustodia",
                        leafHash,
                        "hashCalculado",
                        hashCalculado,
                        "anchoringSimulated",
                        anchorResult.simulated(),
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
}
