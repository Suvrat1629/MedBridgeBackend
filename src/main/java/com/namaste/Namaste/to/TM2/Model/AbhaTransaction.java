package com.namaste.Namaste.to.TM2.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "abha_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbhaTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String txnId;

    private String healthId;
    private String mobile;
    private String transactionType; // REGISTRATION, LOGIN, PROFILE_UPDATE, etc.
    private String status; // PENDING, COMPLETED, FAILED, EXPIRED

    @Column(columnDefinition = "TEXT")
    private String requestData;

    @Column(columnDefinition = "TEXT")
    private String responseData;

    private String errorCode;
    private String errorMessage;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt;

    public AbhaTransaction(String txnId, String transactionType) {
        this.txnId = txnId;
        this.transactionType = transactionType;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(10); // Default 10 minutes expiry
    }
}
