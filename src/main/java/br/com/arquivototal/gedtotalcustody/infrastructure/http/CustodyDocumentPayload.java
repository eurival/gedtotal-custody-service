package br.com.arquivototal.gedtotalcustody.infrastructure.http;

import java.util.Map;

public record CustodyDocumentPayload(
    Long arquivoId,
    Long sourceArquivoId,
    Long masterDadosIndexacaoId,
    Long tenantRootId,
    Long clienteId,
    Long clientePaiId,
    Long departamentoId,
    Long projetoId,
    Long formularioId,
    String nomeArquivo,
    String hashAtual,
    String downloadUrl,
    Map<String, Object> configuracao
) {
}
