package com.namaste.Namaste.to.TM2.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "abha_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbhaUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String healthId;

    @Column(unique = true)
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

    @Column(columnDefinition = "TEXT")
    private String encryptedAadhaar;

    private boolean isActive = true;
    private boolean mobileVerified = false;
    private boolean emailVerified = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
