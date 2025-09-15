package com.namaste.Namaste.to.TM2.Controller;

import com.namaste.Namaste.to.TM2.Model.AbhaProfile;
import com.namaste.Namaste.to.TM2.Request.AbhaLoginRequest;
import com.namaste.Namaste.to.TM2.Request.AbhaRegistrationRequest;
import com.namaste.Namaste.to.TM2.Request.OtpVerificationRequest;
import com.namaste.Namaste.to.TM2.Response.AbhaResponse;
import com.namaste.Namaste.to.TM2.Service.AbhaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/abha")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AbhaController {

    private final AbhaService abhaService;

    // Initialize ABHA service
    @PostMapping("/initialize")
    public Mono<ResponseEntity<AbhaResponse<String>>> initialize() {
        return abhaService.initialize()
                .map(result -> ResponseEntity.ok(AbhaResponse.success("ABHA service initialized successfully")))
                .onErrorReturn(ResponseEntity.ok(AbhaResponse.error("Failed to initialize ABHA service", "INIT_ERROR")));
    }

    // Check if Health ID exists
    @GetMapping("/check/{healthId}")
    public Mono<ResponseEntity<AbhaResponse<Boolean>>> checkHealthIdExists(@PathVariable String healthId) {
        return abhaService.checkHealthIdExists(healthId)
                .map(ResponseEntity::ok);
    }

    // Start registration with Aadhaar
    @PostMapping("/register/aadhaar/start")
    public Mono<ResponseEntity<AbhaResponse<String>>> startRegistrationWithAadhaar(
            @RequestBody @Valid Map<String, String> request) {
        String aadhaar = request.get("aadhaar");
        return abhaService.startRegistrationWithAadhaar(aadhaar)
                .map(ResponseEntity::ok);
    }

    // Verify Aadhaar OTP for registration
    @PostMapping("/register/aadhaar/verify-otp")
    public Mono<ResponseEntity<AbhaResponse<String>>> verifyAadhaarOtp(
            @RequestBody @Valid OtpVerificationRequest request) {
        return abhaService.verifyAadhaarOtpForRegistration(request.getTxnId(), request.getOtp())
                .map(ResponseEntity::ok);
    }

    // Generate Mobile OTP for registration
    @PostMapping("/register/mobile/generate-otp")
    public Mono<ResponseEntity<AbhaResponse<String>>> generateMobileOtp(
            @RequestBody @Valid Map<String, String> request) {
        String txnId = request.get("txnId");
        String mobile = request.get("mobile");
        return abhaService.generateMobileOtpForRegistration(txnId, mobile)
                .map(ResponseEntity::ok);
    }

    // Complete registration
    @PostMapping("/register/complete")
    public Mono<ResponseEntity<AbhaResponse<AbhaProfile>>> completeRegistration(
            @RequestBody @Valid Map<String, Object> request) {
        String txnId = (String) request.get("txnId");
        String otp = (String) request.get("otp");

        // Create registration request from the provided data
        AbhaRegistrationRequest registrationRequest = new AbhaRegistrationRequest();
        registrationRequest.setName((String) request.get("name"));
        registrationRequest.setMobile((String) request.get("mobile"));
        registrationRequest.setEmail((String) request.get("email"));
        registrationRequest.setDateOfBirth((String) request.get("dateOfBirth"));
        registrationRequest.setGender((String) request.get("gender"));
        registrationRequest.setAddress((String) request.get("address"));
        registrationRequest.setState((String) request.get("state"));
        registrationRequest.setDistrict((String) request.get("district"));
        registrationRequest.setPincode((String) request.get("pincode"));

        return abhaService.verifyMobileOtpAndCompleteRegistration(txnId, otp, registrationRequest)
                .map(ResponseEntity::ok);
    }

    // Login with Health ID
    @PostMapping("/login")
    public Mono<ResponseEntity<AbhaResponse<String>>> login(
            @RequestBody @Valid AbhaLoginRequest request) {
        return abhaService.loginWithHealthId(request)
                .map(ResponseEntity::ok);
    }

    // Verify OTP for login
    @PostMapping("/login/verify-otp")
    public Mono<ResponseEntity<AbhaResponse<AbhaProfile>>> verifyLoginOtp(
            @RequestBody @Valid Map<String, String> request) {
        String txnId = request.get("txnId");
        String otp = request.get("otp");
        String authMethod = request.get("authMethod");

        return abhaService.verifyOtpForLogin(txnId, otp, authMethod)
                .map(ResponseEntity::ok);
    }

    // Get profile
    @GetMapping("/profile")
    public Mono<ResponseEntity<AbhaResponse<AbhaProfile>>> getProfile(
            @RequestHeader("X-Auth-Token") String authToken) {
        return abhaService.getProfile(authToken)
                .map(ResponseEntity::ok);
    }

    // Get QR Code
    @GetMapping("/qr-code")
    public Mono<ResponseEntity<AbhaResponse<byte[]>>> getQrCode(
            @RequestHeader("X-Auth-Token") String authToken) {
        return abhaService.getQrCode(authToken)
                .map(ResponseEntity::ok);
    }
}
