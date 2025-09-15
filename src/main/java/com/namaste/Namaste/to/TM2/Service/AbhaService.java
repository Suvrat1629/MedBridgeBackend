package com.namaste.Namaste.to.TM2.Service;

import com.namaste.Namaste.to.TM2.Model.AbhaProfile;
import com.namaste.Namaste.to.TM2.Model.AbhaTransaction;
import com.namaste.Namaste.to.TM2.Model.AbhaUser;
import com.namaste.Namaste.to.TM2.Repository.AbhaTransactionRepository;
import com.namaste.Namaste.to.TM2.Repository.AbhaUserRepository;
import com.namaste.Namaste.to.TM2.Request.AbhaLoginRequest;
import com.namaste.Namaste.to.TM2.Request.AbhaRegistrationRequest;
import com.namaste.Namaste.to.TM2.Response.AbhaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbhaService {

    private final AbhaApiService abhaApiService;
    private final AbhaUserRepository abhaUserRepository;
    private final AbhaTransactionRepository abhaTransactionRepository;
    private final EncryptionService encryptionService;

    // Initialize service by fetching certificate
    public Mono<String> initialize() {
        return abhaApiService.getAuthCertificate();
    }

    // Check if Health ID exists
    public Mono<AbhaResponse<Boolean>> checkHealthIdExists(String healthId) {
        return abhaApiService.checkHealthIdExists(healthId)
                .map(response -> {
                    Boolean exists = (Boolean) response.get("status");
                    return AbhaResponse.success(exists != null ? exists : false);
                })
                .onErrorReturn(AbhaResponse.error("Failed to check Health ID existence", "SYSTEM_ERROR"));
    }

    // Start ABHA registration with Aadhaar
    public Mono<AbhaResponse<String>> startRegistrationWithAadhaar(String aadhaar) {
        log.info("Starting ABHA registration with Aadhaar");

        return abhaApiService.generateAadhaarOtp(aadhaar)
                .map(response -> {
                    String txnId = (String) response.get("txnId");
                    if (txnId != null) {
                        // Save transaction
                        AbhaTransaction transaction = new AbhaTransaction(txnId, "REGISTRATION_AADHAAR_OTP");
                        abhaTransactionRepository.save(transaction);

                        return AbhaResponse.success("OTP sent to registered mobile number", txnId);
                    } else {
                        return AbhaResponse.<String>error("Failed to generate OTP", "OTP_GENERATION_FAILED");
                    }
                })
                .onErrorReturn(AbhaResponse.error("Failed to start registration", "SYSTEM_ERROR"));
    }

    // Verify Aadhaar OTP and proceed with registration
    public Mono<AbhaResponse<String>> verifyAadhaarOtpForRegistration(String txnId, String otp) {
        log.info("Verifying Aadhaar OTP for registration");

        return abhaApiService.verifyAadhaarOtp(txnId, otp)
                .map(response -> {
                    String newTxnId = (String) response.get("txnId");
                    if (newTxnId != null) {
                        // Update transaction
                        abhaTransactionRepository.findByTxnId(txnId)
                                .ifPresent(transaction -> {
                                    transaction.setStatus("AADHAAR_VERIFIED");
                                    abhaTransactionRepository.save(transaction);
                                });

                        return AbhaResponse.success("Aadhaar verified. Please provide mobile number for OTP", newTxnId);
                    } else {
                        return AbhaResponse.<String>error("Invalid OTP", "INVALID_OTP");
                    }
                })
                .onErrorReturn(AbhaResponse.error("OTP verification failed", "VERIFICATION_FAILED"));
    }

    // Generate Mobile OTP for registration
    public Mono<AbhaResponse<String>> generateMobileOtpForRegistration(String txnId, String mobile) {
        log.info("Generating Mobile OTP for registration");

        return abhaApiService.generateMobileOtp(txnId, mobile)
                .map(response -> {
                    String newTxnId = (String) response.get("txnId");
                    if (newTxnId != null) {
                        // Update transaction
                        abhaTransactionRepository.findByTxnId(txnId)
                                .ifPresent(transaction -> {
                                    transaction.setMobile(mobile);
                                    transaction.setStatus("MOBILE_OTP_SENT");
                                    abhaTransactionRepository.save(transaction);
                                });

                        return AbhaResponse.success("OTP sent to mobile number", newTxnId);
                    } else {
                        return AbhaResponse.<String>error("Failed to send mobile OTP", "MOBILE_OTP_FAILED");
                    }
                })
                .onErrorReturn(AbhaResponse.error("Failed to generate mobile OTP", "SYSTEM_ERROR"));
    }

    // Verify Mobile OTP and complete registration
    @Transactional
    public Mono<AbhaResponse<AbhaProfile>> verifyMobileOtpAndCompleteRegistration(
            String txnId, String otp, AbhaRegistrationRequest request) {
        log.info("Verifying Mobile OTP and completing registration");

        return abhaApiService.verifyMobileOtp(txnId, otp)
                .flatMap(verifyResponse -> {
                    String newTxnId = (String) verifyResponse.get("txnId");
                    if (newTxnId == null) {
                        return Mono.just(AbhaResponse.<AbhaProfile>error("Invalid mobile OTP", "INVALID_OTP"));
                    }

                    // Create profile data
                    Map<String, Object> profileData = Map.of(
                            "healthId", request.getName() != null ? request.getName().toLowerCase().replaceAll("\\s+", "") + "." + System.currentTimeMillis() : "",
                            "password", "TempPass@123" // Should be user provided
                    );

                    return abhaApiService.createHealthIdWithPreVerified(newTxnId, profileData)
                            .map(createResponse -> {
                                try {
                                    // Extract profile information
                                    AbhaProfile profile = extractProfileFromResponse(createResponse);

                                    // Save user to database
                                    AbhaUser user = new AbhaUser();
                                    user.setHealthId(profile.getHealthId());
                                    user.setHealthIdNumber(profile.getHealthIdNumber());
                                    user.setName(profile.getName());
                                    user.setMobile(profile.getMobile());
                                    user.setEmail(profile.getEmail());
                                    user.setDateOfBirth(profile.getDateOfBirth());
                                    user.setGender(profile.getGender());
                                    user.setAddress(profile.getAddress());
                                    user.setState(profile.getState());
                                    user.setDistrict(profile.getDistrict());
                                    user.setPincode(profile.getPincode());
                                    user.setMobileVerified(true);
                                    user.setEncryptedAadhaar(encryptionService.encryptWithAbhaCertificate(request.getAadhaar()));

                                    abhaUserRepository.save(user);

                                    // Update transaction
                                    abhaTransactionRepository.findByTxnId(txnId)
                                            .ifPresent(transaction -> {
                                                transaction.setStatus("COMPLETED");
                                                transaction.setHealthId(profile.getHealthId());
                                                abhaTransactionRepository.save(transaction);
                                            });

                                    return AbhaResponse.success(profile);
                                } catch (Exception e) {
                                    log.error("Error processing registration response", e);
                                    return AbhaResponse.<AbhaProfile>error("Registration processing failed", "PROCESSING_ERROR");
                                }
                            });
                })
                .onErrorReturn(AbhaResponse.error("Registration failed", "SYSTEM_ERROR"));
    }

    // Login with Health ID
    public Mono<AbhaResponse<String>> loginWithHealthId(AbhaLoginRequest request) {
        log.info("Starting login with Health ID: {}", request.getHealthId());

        // Check if user exists in our database
        return Mono.fromCallable(() -> abhaUserRepository.findByHealthId(request.getHealthId()))
                .flatMap(userOpt -> {
                    if (userOpt.isEmpty()) {
                        return Mono.just(AbhaResponse.<String>error("Health ID not found", "USER_NOT_FOUND"));
                    }

                    return abhaApiService.initiateAuth(request.getHealthId(), request.getAuthMethod())
                            .map(response -> {
                                String txnId = (String) response.get("txnId");
                                if (txnId != null) {
                                    // Save transaction
                                    AbhaTransaction transaction = new AbhaTransaction(txnId, "LOGIN_" + request.getAuthMethod());
                                    transaction.setHealthId(request.getHealthId());
                                    abhaTransactionRepository.save(transaction);

                                    return AbhaResponse.success("OTP sent for authentication", txnId);
                                } else {
                                    return AbhaResponse.<String>error("Failed to initiate login", "LOGIN_INIT_FAILED");
                                }
                            });
                })
                .onErrorReturn(AbhaResponse.error("Login initiation failed", "SYSTEM_ERROR"));
    }

    // Verify OTP for login
    public Mono<AbhaResponse<AbhaProfile>> verifyOtpForLogin(String txnId, String otp, String authMethod) {
        log.info("Verifying OTP for login");

        Mono<Map<String, Object>> verificationCall;
        if ("AADHAAR_OTP".equals(authMethod)) {
            verificationCall = abhaApiService.confirmWithAadhaarOtp(txnId, otp);
        } else if ("MOBILE_OTP".equals(authMethod)) {
            verificationCall = abhaApiService.confirmWithMobileOtp(txnId, otp);
        } else {
            return Mono.just(AbhaResponse.error("Invalid authentication method", "INVALID_AUTH_METHOD"));
        }

        return verificationCall
                .flatMap(response -> {
                    String token = (String) response.get("token");
                    if (token == null) {
                        return Mono.just(AbhaResponse.<AbhaProfile>error("Authentication failed", "AUTH_FAILED"));
                    }

                    // Get profile with the token
                    return abhaApiService.getProfile(token)
                            .map(profileResponse -> {
                                AbhaProfile profile = extractProfileFromResponse(profileResponse);
                                profile.setToken(token);

                                // Update transaction
                                abhaTransactionRepository.findByTxnId(txnId)
                                        .ifPresent(transaction -> {
                                            transaction.setStatus("COMPLETED");
                                            abhaTransactionRepository.save(transaction);
                                        });

                                return AbhaResponse.success(profile);
                            });
                })
                .onErrorReturn(AbhaResponse.error("Login verification failed", "SYSTEM_ERROR"));
    }

    // Get user profile
    public Mono<AbhaResponse<AbhaProfile>> getProfile(String authToken) {
        return abhaApiService.getProfile(authToken)
                .map(response -> {
                    AbhaProfile profile = extractProfileFromResponse(response);
                    return AbhaResponse.success(profile);
                })
                .onErrorReturn(AbhaResponse.error("Failed to get profile", "PROFILE_ERROR"));
    }

    // Get QR Code
    public Mono<AbhaResponse<byte[]>> getQrCode(String authToken) {
        return abhaApiService.getQrCode(authToken)
                .map(qrData -> AbhaResponse.success(qrData))
                .onErrorReturn(AbhaResponse.error("Failed to get QR code", "QR_ERROR"));
    }

    // Helper method to extract profile from API response
    private AbhaProfile extractProfileFromResponse(Map<String, Object> response) {
        AbhaProfile profile = new AbhaProfile();
        profile.setHealthId((String) response.get("healthId"));
        profile.setHealthIdNumber((String) response.get("healthIdNumber"));
        profile.setName((String) response.get("name"));
        profile.setFirstName((String) response.get("firstName"));
        profile.setMiddleName((String) response.get("middleName"));
        profile.setLastName((String) response.get("lastName"));
        profile.setDateOfBirth((String) response.get("dateOfBirth"));
        profile.setGender((String) response.get("gender"));
        profile.setMobile((String) response.get("mobile"));
        profile.setEmail((String) response.get("email"));
        profile.setAddress((String) response.get("address"));
        profile.setState((String) response.get("state"));
        profile.setDistrict((String) response.get("district"));
        profile.setPincode((String) response.get("pincode"));
        profile.setProfilePhoto((String) response.get("profilePhoto"));
        profile.setMobileVerified((Boolean) response.getOrDefault("mobileVerified", false));
        profile.setEmailVerified((Boolean) response.getOrDefault("emailVerified", false));
        profile.setKycPhoto((String) response.get("kycPhoto"));
        profile.setAuthMethods((String) response.get("authMethods"));
        return profile;
    }

    // Clean up expired transactions
    @Transactional
    public void cleanupExpiredTransactions() {
        List<AbhaTransaction> expiredTransactions = abhaTransactionRepository
                .findExpiredTransactions(LocalDateTime.now());

        expiredTransactions.forEach(transaction -> {
            transaction.setStatus("EXPIRED");
            abhaTransactionRepository.save(transaction);
        });

        log.info("Cleaned up {} expired transactions", expiredTransactions.size());
    }
}
