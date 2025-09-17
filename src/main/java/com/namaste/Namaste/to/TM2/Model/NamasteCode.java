package com.namaste.Namaste.to.TM2.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "namaste_codes")
@CompoundIndex(name = "namaste_name_idx", def = "{'namasteName': 1}")
@CompoundIndex(name = "namaste_code_idx", def = "{'namasteCode': 1}")
@CompoundIndex(name = "icd11_tm2_code_idx", def = "{'icd11Tm2Code': 1}")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NamasteCode {

    @Id
    private String id;

    // NAMASTE Fields
    @Indexed(unique = true)
    private String namasteCode;

    @Indexed
    private String namasteName;

    private String namasteDescription;

    private String namasteCategory; // Ayurveda, Siddha, Unani

    private String namasteSubCategory;

    // WHO International Terminologies for Ayurveda
    private String whoAyurvedaCode;

    private String whoAyurvedaName;

    private String whoAyurvedaDescription;

    // ICD-11 TM2 (Traditional Medicine Module 2) Fields
    @Indexed
    private String icd11Tm2Code;

    private String icd11Tm2Name;

    private String icd11Tm2Description;

    private String icd11Tm2Uri;

    // ICD-11 Biomedicine Fields (for dual coding)
    private String icd11BiomedicineCode;

    private String icd11BiomedicineName;

    private String icd11BiomedicineDescription;

    private String icd11BiomedicineUri;

    // Mapping Metadata
    private String mappingConfidence; // HIGH, MEDIUM, LOW

    private String mappingType; // EXACT, PARTIAL, BROAD, NARROW

    private String mappingNotes;

    private String mappedBy; // WHO mapped it or system mapped

    // Version and Audit Fields
    private String namasteVersion = "1.0.0";

    private String icd11Version = "2024-01";

    private boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    private String createdBy;

    private String updatedBy;

    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to get display name for UI
    public String getDisplayName() {
        return namasteName + " (" + namasteCode + ")";
    }

    // Helper method to check if dual coding is available
    public boolean hasDualCoding() {
        return icd11Tm2Code != null && icd11BiomedicineCode != null;
    }

    // Helper method to get all ICD-11 codes as a combined string
    public String getAllIcd11Codes() {
        StringBuilder sb = new StringBuilder();
        if (icd11Tm2Code != null) {
            sb.append("TM2: ").append(icd11Tm2Code);
        }
        if (icd11BiomedicineCode != null) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Biomedicine: ").append(icd11BiomedicineCode);
        }
        return sb.toString();
    }
}