package com.namaste.Namaste.to.TM2.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AbhaRegistrationRequest {
    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "\\d{12}", message = "Aadhaar number must be 12 digits")
    private String aadhaar;

    @Pattern(regexp = "\\d{10}", message = "Mobile number must be 10 digits")
    private String mobile;

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    private String dateOfBirth;

    @Pattern(regexp = "[MFO]", message = "Gender must be M, F, or O")
    private String gender;

    private String email;

    private String address;

    private String state;

    private String district;

    private String pincode;

    // For biometric registration
    private String pid;
    private String bioType;

    // For document-based registration
    private String documentType;
    private String documentNumber;
    private String documentFrontImage;
    private String documentBackImage;
}
