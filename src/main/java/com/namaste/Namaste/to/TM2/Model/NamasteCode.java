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
}