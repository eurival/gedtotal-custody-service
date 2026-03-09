package br.com.arquivototal.gedtotalcustody.domain.event;

import br.com.arquivototal.gedtotalcustody.domain.enumeration.ProcessingStatus;
import java.util.Map;

public record CustodyResultEvent(
    String jobId,
    Long custodiaDocumentoId,
    Long arquivoId,
    Long masterDadosIndexacaoId,
    Long tenantRootId,
    Long clienteId,
    Long clientePaiId,
    Long departamentoId,
    Long projetoId,
    Long formularioId,
    ProcessingStatus status,
    String rootHash,
    String txHash,
    String network,
    String contractAddress,
    Map<String, Object> metadata,
    String traceId
) {
}
