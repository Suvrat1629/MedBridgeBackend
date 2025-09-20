package com.namaste.Namaste.to.TM2.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.namaste.Namaste.to.TM2.Model.NamasteCode;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Simplified FHIR Terminology Service
 * Focused on: Disease name → ICD-11 codes mapping (NO patient data)
 */
@Service
public class TerminologyFhirService {

    private static final Logger log = LoggerFactory.getLogger(TerminologyFhirService.class);
    private final FhirContext fhirContext;
    private final IParser jsonParser;
    private final NamasteTerminologyService terminologyService;

    public TerminologyFhirService(NamasteTerminologyService terminologyService) {
        this.terminologyService = terminologyService;
        this.fhirContext = FhirContext.forR4();
        this.jsonParser = fhirContext.newJsonParser().setPrettyPrint(true);
    }

    /**
     * Serialize FHIR resource to JSON
     */
    public String toJson(Resource resource) {
        return jsonParser.encodeResourceToString(resource);
    }

    /**
     * Create FHIR Parameters for search by code result - MAIN FEATURE 1
     */
    public Parameters createSearchByCodeResult(String codeValue) {
        log.info("Creating FHIR Parameters for code search: {}", codeValue);

        Parameters parameters = new Parameters();
        parameters.setId("search-by-code-result-" + codeValue);

        // Search using the terminology service
        List<NamasteCode> result = terminologyService.searchByCode(codeValue);

        if (result.isEmpty()) {
            // Result parameter (false = not found)
            parameters.addParameter("result", new BooleanType(false));
            parameters.addParameter("message", new StringType("No code found matching: " + codeValue));
            return parameters;
        }

        // Result parameter (true = found)
        parameters.addParameter("result", new BooleanType(true));
        parameters.addParameter("totalMatches", new IntegerType(result.size()));

        // Process all found codes
        for (int i = 0; i < result.size(); i++) {
            NamasteCode namasteCode = result.get(i);

            // Create a parameter group for each match
            Parameters.ParametersParameterComponent matchGroup = new Parameters.ParametersParameterComponent();
            matchGroup.setName("match");

            // Add found code details
            Parameters.ParametersParameterComponent codeParam = new Parameters.ParametersParameterComponent();
            codeParam.setName("code");
            codeParam.addPart().setName("system").setValue(new UriType("http://terminology.hl7.org.in/CodeSystem/namaste"));
            codeParam.addPart().setName("code").setValue(new CodeType(namasteCode.getNamasteCode()));
            codeParam.addPart().setName("display").setValue(new StringType(namasteCode.getNamasteName()));
            matchGroup.addPart(codeParam);

            // Add type parameter
            matchGroup.addPart().setName("type").setValue(new StringType(namasteCode.getNamasteCategory()));

            // Add TM2 mapping if available
            if (namasteCode.getIcd11Tm2Code() != null) {
                Parameters.ParametersParameterComponent tm2Param = new Parameters.ParametersParameterComponent();
                tm2Param.setName("tm2Mapping");
                tm2Param.addPart().setName("system").setValue(new UriType("http://id.who.int/icd/release/11/tm2"));
                tm2Param.addPart().setName("code").setValue(new CodeType(namasteCode.getIcd11Tm2Code()));
                tm2Param.addPart().setName("display").setValue(new StringType(namasteCode.getIcd11Tm2Name()));
                tm2Param.addPart().setName("definition").setValue(new StringType(namasteCode.getIcd11Tm2Description()));
                tm2Param.addPart().setName("link").setValue(new UriType(namasteCode.getIcd11Tm2Uri()));
                matchGroup.addPart(tm2Param);
            }

            // Add code description and confidence
            if (namasteCode.getNamasteDescription() != null) {
                matchGroup.addPart().setName("description").setValue(new StringType(namasteCode.getNamasteDescription()));
            }

            if (namasteCode.getConfidenceScore() != null) {
                matchGroup.addPart().setName("confidenceScore").setValue(new DecimalType(namasteCode.getConfidenceScore()));
            }

            parameters.addParameter(matchGroup);
        }

        return parameters;
    }

    /**
     * Create FHIR Parameters for search by TM2 code only result
     */
    public Parameters createSearchByTm2CodeResult(String codeValue) {
        log.info("Creating FHIR Parameters for TM2 code search: {}", codeValue);

        Parameters parameters = new Parameters();
        parameters.setId("search-by-tm2code-result-" + codeValue);

        // Search using the terminology service
        List<NamasteCode> result = terminologyService.searchByTm2CodeOnly(codeValue);

        if (result.isEmpty()) {
            // Result parameter (false = not found)
            parameters.addParameter("result", new BooleanType(false));
            parameters.addParameter("message", new StringType("No TM2 code found matching: " + codeValue));
            return parameters;
        }

        // Result parameter (true = found)
        parameters.addParameter("result", new BooleanType(true));
        parameters.addParameter("totalMatches", new IntegerType(result.size()));

        // Process all found codes
        for (int i = 0; i < result.size(); i++) {
            NamasteCode namasteCode = result.get(i);

            // Create a parameter group for each match
            Parameters.ParametersParameterComponent matchGroup = new Parameters.ParametersParameterComponent();
            matchGroup.setName("match");

            // Add found code details
            Parameters.ParametersParameterComponent codeParam = new Parameters.ParametersParameterComponent();
            codeParam.setName("code");
            codeParam.addPart().setName("system").setValue(new UriType("http://terminology.hl7.org.in/CodeSystem/namaste"));
            codeParam.addPart().setName("code").setValue(new CodeType(namasteCode.getNamasteCode()));
            codeParam.addPart().setName("display").setValue(new StringType(namasteCode.getNamasteName()));
            matchGroup.addPart(codeParam);

            // Add type parameter
            matchGroup.addPart().setName("type").setValue(new StringType(namasteCode.getNamasteCategory()));

            // Add TM2 mapping if available
            if (namasteCode.getIcd11Tm2Code() != null) {
                Parameters.ParametersParameterComponent tm2Param = new Parameters.ParametersParameterComponent();
                tm2Param.setName("tm2Mapping");
                tm2Param.addPart().setName("system").setValue(new UriType("http://id.who.int/icd/release/11/tm2"));
                tm2Param.addPart().setName("code").setValue(new CodeType(namasteCode.getIcd11Tm2Code()));
                tm2Param.addPart().setName("display").setValue(new StringType(namasteCode.getIcd11Tm2Name()));
                tm2Param.addPart().setName("definition").setValue(new StringType(namasteCode.getIcd11Tm2Description()));
                tm2Param.addPart().setName("link").setValue(new UriType(namasteCode.getIcd11Tm2Uri()));
                matchGroup.addPart(tm2Param);
            }

            // Add code description and confidence
            if (namasteCode.getNamasteDescription() != null) {
                matchGroup.addPart().setName("description").setValue(new StringType(namasteCode.getNamasteDescription()));
            }

            if (namasteCode.getConfidenceScore() != null) {
                matchGroup.addPart().setName("confidenceScore").setValue(new DecimalType(namasteCode.getConfidenceScore()));
            }

            parameters.addParameter(matchGroup);
        }

        return parameters;
    }

    /**
     * Create FHIR Parameters for search by code only result
     */
    public Parameters createSearchByCodeOnlyResult(String codeValue) {
        log.info("Creating FHIR Parameters for code only search: {}", codeValue);

        Parameters parameters = new Parameters();
        parameters.setId("search-by-codeonly-result-" + codeValue);

        // Search using the terminology service
        List<NamasteCode> result = terminologyService.searchByCodeOnly(codeValue);

        if (result.isEmpty()) {
            // Result parameter (false = not found)
            parameters.addParameter("result", new BooleanType(false));
            parameters.addParameter("message", new StringType("No code found matching: " + codeValue));
            return parameters;
        }

        // Result parameter (true = found)
        parameters.addParameter("result", new BooleanType(true));
        parameters.addParameter("totalMatches", new IntegerType(result.size()));

        // Process all found codes
        for (int i = 0; i < result.size(); i++) {
            NamasteCode namasteCode = result.get(i);

            // Create a parameter group for each match
            Parameters.ParametersParameterComponent matchGroup = new Parameters.ParametersParameterComponent();
            matchGroup.setName("match");

            // Add found code details
            Parameters.ParametersParameterComponent codeParam = new Parameters.ParametersParameterComponent();
            codeParam.setName("code");
            codeParam.addPart().setName("system").setValue(new UriType("http://terminology.hl7.org.in/CodeSystem/namaste"));
            codeParam.addPart().setName("code").setValue(new CodeType(namasteCode.getNamasteCode()));
            codeParam.addPart().setName("display").setValue(new StringType(namasteCode.getNamasteName()));
            matchGroup.addPart(codeParam);

            // Add type parameter
            matchGroup.addPart().setName("type").setValue(new StringType(namasteCode.getNamasteCategory()));

            // Add TM2 mapping if available
            if (namasteCode.getIcd11Tm2Code() != null) {
                Parameters.ParametersParameterComponent tm2Param = new Parameters.ParametersParameterComponent();
                tm2Param.setName("tm2Mapping");
                tm2Param.addPart().setName("system").setValue(new UriType("http://id.who.int/icd/release/11/tm2"));
                tm2Param.addPart().setName("code").setValue(new CodeType(namasteCode.getIcd11Tm2Code()));
                tm2Param.addPart().setName("display").setValue(new StringType(namasteCode.getIcd11Tm2Name()));
                tm2Param.addPart().setName("definition").setValue(new StringType(namasteCode.getIcd11Tm2Description()));
                tm2Param.addPart().setName("link").setValue(new UriType(namasteCode.getIcd11Tm2Uri()));
                matchGroup.addPart(tm2Param);
            }

            // Add code description and confidence
            if (namasteCode.getNamasteDescription() != null) {
                matchGroup.addPart().setName("description").setValue(new StringType(namasteCode.getNamasteDescription()));
            }

            if (namasteCode.getConfidenceScore() != null) {
                matchGroup.addPart().setName("confidenceScore").setValue(new DecimalType(namasteCode.getConfidenceScore()));
            }

            parameters.addParameter(matchGroup);
        }

        return parameters;
    }

    /**
     * Create FHIR Parameters for search by symptoms result - MAIN FEATURE 2
     * Modified to use grouped results and show proper disease groupings
     */
    public Parameters createSearchBySymptomsResult(List<String> symptoms) {
        log.info("Creating FHIR Parameters for symptoms search: {}", symptoms);

        if (symptoms == null || symptoms.isEmpty()) {
            // Create empty parameters if no symptoms provided
            Parameters parameters = new Parameters();
            parameters.setId("search-by-symptoms-result-" + System.currentTimeMillis());
            parameters.addParameter("result", new BooleanType(false));
            parameters.addParameter("message", new StringType("No symptoms provided"));
            return parameters;
        }

        // Search using the NEW GROUPED terminology service method
        List<NamasteTerminologyService.DiseaseMapping> groupedResults = terminologyService.searchBySymptomsGrouped(symptoms);

        if (groupedResults.isEmpty()) {
            // Create empty parameters if no results found
            Parameters parameters = new Parameters();
            parameters.setId("search-by-symptoms-result-" + System.currentTimeMillis());
            parameters.addParameter("result", new BooleanType(false));
            parameters.addParameter("message", new StringType("No symptoms found matching: " + String.join(", ", symptoms)));
            return parameters;
        }

        // Check if results exceed 20 DISEASE GROUPS (not individual mappings)
        if (groupedResults.size() > 20) {
            Parameters parameters = new Parameters();
            parameters.setId("search-by-symptoms-error-" + System.currentTimeMillis());
            parameters.addParameter("result", new BooleanType(false));
            parameters.addParameter("error", new StringType("Too many results"));
            parameters.addParameter("message", new StringType("Found " + groupedResults.size() + " disease groups. Please refine your symptoms to get 20 or fewer results."));
            parameters.addParameter("resultCount", new IntegerType(groupedResults.size()));
            parameters.addParameter("maxAllowed", new IntegerType(20));
            return parameters;
        }

        // Return ALL grouped results
        Parameters parameters = new Parameters();
        parameters.setId("search-by-symptoms-grouped-results-" + System.currentTimeMillis());
        parameters.addParameter("result", new BooleanType(true));
        parameters.addParameter("totalDiseaseGroups", new IntegerType(groupedResults.size()));
        parameters.addParameter("matchedSymptoms", new StringType(String.join(", ", symptoms)));

        // Add each disease group as a parameter
        for (int i = 0; i < groupedResults.size(); i++) {
            NamasteTerminologyService.DiseaseMapping diseaseMapping = groupedResults.get(i);

            // Create a parameter group for each disease
            Parameters.ParametersParameterComponent diseaseGroup = new Parameters.ParametersParameterComponent();
            diseaseGroup.setName("diseaseGroup");

            // Add TM2 disease information
            Parameters.ParametersParameterComponent tm2Info = new Parameters.ParametersParameterComponent();
            tm2Info.setName("tm2Disease");
            tm2Info.addPart().setName("system").setValue(new UriType("http://id.who.int/icd/release/11/tm2"));
            tm2Info.addPart().setName("code").setValue(new CodeType(diseaseMapping.getTm2Code()));
            tm2Info.addPart().setName("display").setValue(new StringType(diseaseMapping.getTm2Title()));
            if (diseaseMapping.getTm2Definition() != null) {
                tm2Info.addPart().setName("definition").setValue(new StringType(diseaseMapping.getTm2Definition()));
            }
            diseaseGroup.addPart(tm2Info);

            // Add symptom similarity score
            if (diseaseMapping.getSimilarityScore() != null) {
                diseaseGroup.addPart().setName("symptomSimilarityScore").setValue(new DecimalType(diseaseMapping.getSimilarityScore()));
            }

            // Add count of traditional medicine mappings
            diseaseGroup.addPart().setName("traditionalMedicineMappingCount").setValue(new IntegerType(diseaseMapping.getMappingCount()));

            // Add all traditional medicine mappings for this disease
            List<NamasteCode> mappings = diseaseMapping.getMappings();
            for (NamasteCode mapping : mappings) {
                Parameters.ParametersParameterComponent mappingParam = new Parameters.ParametersParameterComponent();
                mappingParam.setName("traditionalMedicineMapping");

                // Add traditional medicine code details
                Parameters.ParametersParameterComponent codeParam = new Parameters.ParametersParameterComponent();
                codeParam.setName("code");
                codeParam.addPart().setName("system").setValue(new UriType("http://terminology.hl7.org.in/CodeSystem/namaste"));
                codeParam.addPart().setName("code").setValue(new CodeType(mapping.getNamasteCode()));
                codeParam.addPart().setName("display").setValue(new StringType(mapping.getNamasteName()));
                mappingParam.addPart(codeParam);

                // Add type (ayurveda, siddha, unani)
                mappingParam.addPart().setName("type").setValue(new StringType(mapping.getNamasteCategory()));

                // Add description if available
                if (mapping.getNamasteDescription() != null) {
                    mappingParam.addPart().setName("description").setValue(new StringType(mapping.getNamasteDescription()));
                }

                // Add mapping confidence score
                if (mapping.getConfidenceScore() != null) {
                    mappingParam.addPart().setName("mappingConfidenceScore").setValue(new DecimalType(mapping.getConfidenceScore()));
                }

                diseaseGroup.addPart(mappingParam);
            }

            parameters.addParameter(diseaseGroup);
        }

        return parameters;
    }
}