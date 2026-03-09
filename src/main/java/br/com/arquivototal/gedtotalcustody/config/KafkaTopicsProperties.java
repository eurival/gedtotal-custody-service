package br.com.arquivototal.gedtotalcustody.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProperties(
    String custodyRequest,
    String custodyResult,
    String custodyFailure
) {
}
