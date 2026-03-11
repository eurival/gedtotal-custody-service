package br.com.arquivototal.gedtotalcustody.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.internal-api")
public record InternalApiProperties(String gedtotalapiBaseUrl, String internalToken) {
}
