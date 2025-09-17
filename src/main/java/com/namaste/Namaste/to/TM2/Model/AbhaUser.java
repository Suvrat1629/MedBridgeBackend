package com.namaste.Namaste.to.TM2.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "abha_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbhaUser {
    @Id
    private String id;

    @Indexed(unique = true)
    private String healthId;

    @Indexed(unique = true)
    private String healthIdNumber;

    private String name;
    private String mobile;
    private String email;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String state;
    private String district;
    private String pincode;

    private String encryptedAadhaar;

    private boolean isActive = true;
    private boolean mobileVerified = false;
    private boolean emailVerified = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
