package com.namaste.Namaste.to.TM2.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "tm2_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NamasteCode {

    @Id
    private String id;

    @Field("tm2_code")
    @Indexed
    private String tm2Code;

    @Field("tm2_link")
    private String tm2Link;

    @Field("code")
    @Indexed
    private String code;

    @Field("tm2_title")
    @Indexed
    private String tm2Title;

    @Field("tm2_definition")
    private String tm2Definition;

    @Field("code_title")
    @Indexed
    private String codeTitle;

    @Field("code_description")
    private String codeDescription;

    @Field("confidence_score")
    private Double confidenceScore;

    @Field("type")
    @Indexed
    private String type;

    // Helper methods for backward compatibility with FHIR service
    public String getNamasteCode() {
        return this.code;
    }

    public String getNamasteName() {
        return this.codeTitle;
    }

    public String getNamasteDescription() {
        return this.codeDescription;
    }

    public String getNamasteCategory() {
        return this.type;
    }

    public String getIcd11Tm2Code() {
        return this.tm2Code;
    }

    public String getIcd11Tm2Name() {
        return this.tm2Title;
    }

    public String getIcd11Tm2Description() {
        return this.tm2Definition;
    }

    public String getIcd11Tm2Uri() {
        return this.tm2Link;
    }

    // For dual coding support - these fields are not in your current document
    // but keeping for FHIR compatibility
    public String getIcd11BiomedicineCode() {
        return null; // Not available in current document structure
    }

    public String getIcd11BiomedicineName() {
        return null; // Not available in current document structure
    }

    public String getIcd11BiomedicineDescription() {
        return null; // Not available in current document structure
    }

    public String getIcd11BiomedicineUri() {
        return null; // Not available in current document structure
    }

    // Helper methods
    public String getDisplayName() {
        return codeTitle + " (" + code + ")";
    }

    public boolean hasDualCoding() {
        return false; // Not supported with current document structure
    }

    public String getAllIcd11Codes() {
        return tm2Code != null ? "TM2: " + tm2Code : "";
    }

    public String getMappingConfidence() {
        if (confidenceScore != null) {
            if (confidenceScore >= 0.8) return "HIGH";
            if (confidenceScore >= 0.6) return "MEDIUM";
            return "LOW";
        }
        return "UNKNOWN";
    }

    // For FHIR compatibility
    public boolean isActive() {
        return true; // Assuming all records in collection are active
    }
}