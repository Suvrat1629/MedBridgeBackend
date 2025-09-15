package com.namaste.Namaste.to.TM2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "abha")
@Data
public class AbhaConfig {
    private String baseUrl = "https://sandbox.abdm.gov.in";
    private String clientId;
    private String clientSecret;
    private String xHipId;
    private String authToken;
    private int otpValidityMinutes = 10;
    private int maxRetryAttempts = 3;
    private int connectionTimeoutSeconds = 30;
    private int readTimeoutSeconds = 60;
}
