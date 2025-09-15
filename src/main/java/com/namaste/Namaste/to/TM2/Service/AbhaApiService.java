package com.namaste.Namaste.to.TM2.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namaste.Namaste.to.TM2.config.AbhaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class AbhaApiService {

    private final AbhaConfig abhaConfig;
    private final ObjectMapper objectMapper;
    private final EncryptionService encryptionService;
    private final WebClient webClient;

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE_REF = 
            new ParameterizedTypeReference<Map<String, Object>>() {};

    public AbhaApiService(AbhaConfig abhaConfig, ObjectMapper objectMapper, 
                         EncryptionService encryptionService) {
        this.abhaConfig = abhaConfig;
        this.objectMapper = objectMapper;
        this.encryptionService = encryptionService;
        this.webClient = WebClient.builder()
                .baseUrl(abhaConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-HIP-ID", abhaConfig.getXHipId())
                .build();
    }

    // Get authentication certificate
    public Mono<String> getAuthCertificate() {
        return webClient.get()
                .uri("/v2/auth/cert")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(abhaConfig.getConnectionTimeoutSeconds()))
                .doOnSuccess(cert -> {
                    log.info("Successfully retrieved ABHA certificate");
                    encryptionService.setAbhaCertificate(cert);
                })
                .doOnError(error -> log.error("Failed to retrieve ABHA certificate", error));
    }

    // Check if Health ID exists
    public Mono<Map<String, Object>> checkHealthIdExists(String healthId) {
        return webClient.get()
                .uri("/v1/search/existsByHealthId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .header("healthId", healthId)
                .retrieve()
                .bodyToMono(MAP_TYPE_REF)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to check Health ID existence", error));
    }

    // Search Health ID by Health ID
    public Mono<Map<String, Object>> searchByHealthId(String healthId) {
        return webClient.get()
                .uri("/v1/search/searchByHealthId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .header("healthId", healthId)
                .retrieve()
                .bodyToMono(MAP_TYPE_REF)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to search by Health ID", error));
    }

    // Generate Aadhaar OTP for registration
    public Mono<Map<String, Object>> generateAadhaarOtp(String aadhaar) {
        Map<String, Object> request = Map.of(
                "aadhaar", encryptionService.encryptWithAbhaCertificate(aadhaar)
        );

        return webClient.post()
                .uri("/v1/registration/aadhaar/generateOtp")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MAP_TYPE_REF)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to generate Aadhaar OTP", error));
    }

    // Verify Aadhaar OTP for registration
    public Mono<Map<String, Object>> verifyAadhaarOtp(String txnId, String otp) {
        Map<String, Object> request = Map.of(
                "otp", encryptionService.encryptWithAbhaCertificate(otp),
                "txnId", txnId
        );

        return webClient.post()
                .uri("/v1/registration/aadhaar/verifyOTP")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MAP_TYPE_REF)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to verify Aadhaar OTP", error));
    }

    // Generate Mobile OTP for registration
    public Mono<Map<String, Object>> generateMobileOtp(String txnId, String mobile) {
        Map<String, Object> request = Map.of(
                "mobile", mobile,
                "txnId", txnId
        );

        return webClient.post()
                .uri("/v1/registration/aadhaar/generateMobileOTP")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MAP_TYPE_REF)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to generate Mobile OTP", error));
    }

    // Verify Mobile OTP for registration
    public Mono<Map<String, Object>> verifyMobileOtp(String txnId, String otp) {
        Map<String, Object> request = Map.of(
                "otp", encryptionService.encryptWithAbhaCertificate(otp),
                "txnId", txnId
        );

        return webClient.post()
                .uri("/v1/registration/aadhaar/verifyMobileOTP")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MAP_TYPE_REF)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to verify Mobile OTP", error));
    }

    // Create Health ID with pre-verified Aadhaar and Mobile
    public Mono<Map<String, Object>> createHealthIdWithPreVerified(String txnId, Map<String, Object> profileData) {
        Map<String, Object> request = Map.of(
                "txnId", txnId,
                "healthId", profileData.getOrDefault("healthId", ""),
                "password", encryptionService.encryptWithAbhaCertificate((String) profileData.getOrDefault("password", "")),
                "consent", true,
                "consentVersion", "1.4"
        );

        return webClient.post()
                .uri("/v1/registration/aadhaar/createHealthIdWithPreVerified")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MAP_TYPE_REF)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to create Health ID", error));
    }

    // Initiate authentication
    public Mono<Map<String, Object>> initiateAuth(String healthId, String authMethod) {
        Map<String, Object> request = Map.of(
                "healthId", healthId,
                "authMethod", authMethod
        );

        return webClient.post()
                .uri("/v2/auth/init")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MAP_TYPE_REF)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to initiate authentication", error));
    }

    // Confirm authentication with Aadhaar OTP
    public Mono<Map<String, Object>> confirmWithAadhaarOtp(String txnId, String otp) {
        Map<String, Object> request = Map.of(
                "otp", encryptionService.encryptWithAbhaCertificate(otp),
                "txnId", txnId
        );

        return webClient.post()
                .uri("/v1/auth/confirmWithAadhaarOtp")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MAP_TYPE_REF)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to confirm Aadhaar OTP authentication", error));
    }

    // Confirm authentication with Mobile OTP
    public Mono<Map<String, Object>> confirmWithMobileOtp(String txnId, String otp) {
        Map<String, Object> request = Map.of(
                "otp", encryptionService.encryptWithAbhaCertificate(otp),
                "txnId", txnId
        );

        return webClient.post()
                .uri("/v1/auth/confirmWithMobileOTP")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MAP_TYPE_REF)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to confirm Mobile OTP authentication", error));
    }

    // Get profile
    public Mono<Map<String, Object>> getProfile(String authToken) {
        return webClient.get()
                .uri("/v1/account/profile")
                .header("X-Token", authToken)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .retrieve()
                .bodyToMono(MAP_TYPE_REF)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to get profile", error));
    }

    // Get QR Code
    public Mono<byte[]> getQrCode(String authToken) {
        return webClient.get()
                .uri("/v1/account/qrCode")
                .header("X-Token", authToken)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + abhaConfig.getAuthToken())
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(abhaConfig.getReadTimeoutSeconds()))
                .doOnError(error -> log.error("Failed to get QR code", error));
    }

    // Generic API call method for handling errors
    private <T> Mono<T> handleApiCall(Mono<T> apiCall, String operation) {
        return apiCall
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("API call failed for {}: Status {}, Body: {}", 
                            operation, ex.getStatusCode(), ex.getResponseBodyAsString());
                    return Mono.error(new RuntimeException("ABHA API Error: " + ex.getMessage()));
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Unexpected error during {}: {}", operation, ex.getMessage());
                    return Mono.error(new RuntimeException("System Error: " + ex.getMessage()));
                });
    }
}
