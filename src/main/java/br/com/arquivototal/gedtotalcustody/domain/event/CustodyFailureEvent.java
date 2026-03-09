package br.com.arquivototal.gedtotalcustody.domain.event;

import br.com.arquivototal.gedtotalcustody.domain.enumeration.CustodyStepType;

public record CustodyFailureEvent(
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
    CustodyStepType etapa,
    String errorCode,
    String errorMessage,
    String traceId
) {
}
