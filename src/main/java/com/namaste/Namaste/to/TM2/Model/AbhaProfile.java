package com.namaste.Namaste.to.TM2.Model;

import lombok.Data;

@Data
public class AbhaProfile {
    private String healthId;
    private String healthIdNumber;
    private String name;
    private String firstName;
    private String middleName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String mobile;
    private String email;
    private String address;
    private String state;
    private String district;
    private String pincode;
    private String profilePhoto;
    private boolean mobileVerified;
    private boolean emailVerified;
    private String kycPhoto;
    private String authMethods;
    private String token;
}
