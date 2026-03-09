package br.com.arquivototal.gedtotalcustody.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(boolean oauth2Enabled) {
}
