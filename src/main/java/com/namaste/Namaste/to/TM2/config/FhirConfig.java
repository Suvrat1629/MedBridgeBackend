package com.namaste.Namaste.to.TM2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fhir.server")
@Data
public class FhirConfig {

    private String baseUrl = "http://localhost:8080/fhir";
    private String name = "NAMASTE-TM2 FHIR Terminology Server";
    private String version = "1.0.0";
    private String implementationDescription = "FHIR R4 Terminology Server for NAMASTE to ICD-11 TM2 mapping";
}