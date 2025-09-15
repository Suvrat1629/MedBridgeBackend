package com.namaste.Namaste.to.TM2.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerificationRequest {
    @NotBlank(message = "Transaction ID is required")
    private String txnId;

    @NotBlank(message = "OTP is required")
    private String otp;
}
