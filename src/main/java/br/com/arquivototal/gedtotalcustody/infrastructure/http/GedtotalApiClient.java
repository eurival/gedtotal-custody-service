package br.com.arquivototal.gedtotalcustody.infrastructure.http;

import br.com.arquivototal.gedtotalcustody.config.InternalApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class GedtotalApiClient {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    private final RestClient.Builder restClientBuilder;
    private final InternalApiProperties internalApiProperties;

    public CustodyDocumentPayload fetchPayload(String payloadUrl) {
        log.info("Buscando payload de custodia no gedtotalapi url={}", payloadUrl);
        return restClientBuilder
            .baseUrl(internalApiProperties.gedtotalapiBaseUrl())
            .build()
            .get()
            .uri(payloadUrl)
            .header(INTERNAL_TOKEN_HEADER, internalTokenHeader())
            .retrieve()
            .body(CustodyDocumentPayload.class);
    }

    public byte[] fetchDocumentContent(String contentUrl) {
        log.info("Baixando conteudo do documento para custodia url={}", contentUrl);
        return restClientBuilder
            .baseUrl(internalApiProperties.gedtotalapiBaseUrl())
            .build()
            .get()
            .uri(contentUrl)
            .header(INTERNAL_TOKEN_HEADER, internalTokenHeader())
            .retrieve()
            .body(byte[].class);
    }

    private String internalTokenHeader() {
        String token = internalApiProperties.internalToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("app.internal-api.internal-token nao configurado");
        }
        return token;
    }
}
