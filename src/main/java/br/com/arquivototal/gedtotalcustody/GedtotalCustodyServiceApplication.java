package br.com.arquivototal.gedtotalcustody;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry
public class GedtotalCustodyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GedtotalCustodyServiceApplication.class, args);
    }
}
