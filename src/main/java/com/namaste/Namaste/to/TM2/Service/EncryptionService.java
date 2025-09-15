package com.namaste.Namaste.to.TM2.Service;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionService {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private PublicKey abhaCertificatePublicKey;

    public void setAbhaCertificate(String certificateString) {
        try {
            // Remove header and footer if present
            String cleanCert = certificateString
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s+", "");

            byte[] certBytes = Base64.getDecoder().decode(cleanCert);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(certBytes)
            );

            this.abhaCertificatePublicKey = certificate.getPublicKey();
            log.info("ABHA certificate loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load ABHA certificate", e);
            throw new RuntimeException("Failed to load ABHA certificate", e);
        }
    }

    public String encryptWithAbhaCertificate(String data) {
        if (abhaCertificatePublicKey == null) {
            throw new IllegalStateException("ABHA certificate not loaded");
        }

        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, abhaCertificatePublicKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    public String generateUUID() {
        return java.util.UUID.randomUUID().toString();
    }
}
