package com.namaste.Namaste.to.TM2.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AbhaLoginRequest {
    @NotBlank(message = "Health ID is required")
    private String healthId;

    private String authMethod; // "AADHAAR_OTP" or "MOBILE_OTP"

    // For mobile login
    private String mobile;
}
