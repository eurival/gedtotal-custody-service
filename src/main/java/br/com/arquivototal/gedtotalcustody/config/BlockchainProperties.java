package br.com.arquivototal.gedtotalcustody.config;

import java.math.BigInteger;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.blockchain")
public record BlockchainProperties(
    boolean enabled,
    String network,
    String rpcUrl,
    Long chainId,
    String privateKey,
    String anchorAddress,
    BigInteger gasPriceWei,
    BigInteger gasLimit
) {}
