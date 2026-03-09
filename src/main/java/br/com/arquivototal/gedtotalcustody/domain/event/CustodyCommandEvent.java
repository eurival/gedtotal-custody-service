package br.com.arquivototal.gedtotalcustody.domain.event;

public record CustodyCommandEvent(
    String jobId,
    Long custodiaDocumentoId,
    Long arquivoId,
    Long sourceArquivoId,
    Long masterDadosIndexacaoId,
    Long tenantRootId,
    Long clienteId,
    Long clientePaiId,
    Long departamentoId,
    Long projetoId,
    Long formularioId,
    String hashFinalDocumento,
    String traceId
) {
}
