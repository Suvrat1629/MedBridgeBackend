package com.namaste.Namaste.to.TM2.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "abha_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbhaTransaction {
    @Id
    private String id;

    @Indexed(unique = true)
    private String txnId;

    private String healthId;
    private String mobile;
    private String transactionType; // REGISTRATION, LOGIN, PROFILE_UPDATE, etc.
    private String status; // PENDING, COMPLETED, FAILED, EXPIRED

    private String requestData;
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
