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
     * Modified to return error if > 20 results, otherwise return all results without filtering
     * Filtering code kept but unused for future reference
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

        // Search using the terminology service (results are already ordered by symptom relevance)
        List<NamasteCode> results = terminologyService.searchBySymptoms(symptoms);

        if (results.isEmpty()) {
            // Create empty parameters if no results found
            Parameters parameters = new Parameters();
            parameters.setId("search-by-symptoms-result-" + System.currentTimeMillis());
            parameters.addParameter("result", new BooleanType(false));
            parameters.addParameter("message", new StringType("No symptoms found matching: " + String.join(", ", symptoms)));
            return parameters;
        }

        // NEW LOGIC: Check if results exceed 20 - return error if so
        if (results.size() > 20) {
            Parameters parameters = new Parameters();
            parameters.setId("search-by-symptoms-error-" + System.currentTimeMillis());
            parameters.addParameter("result", new BooleanType(false));
            parameters.addParameter("error", new StringType("Too many results"));
            parameters.addParameter("message", new StringType("Found " + results.size() + " matches. Please refine your symptoms to get 20 or fewer results."));
            parameters.addParameter("resultCount", new IntegerType(results.size()));
            parameters.addParameter("maxAllowed", new IntegerType(20));
            return parameters;
        }

        // NEW LOGIC: Return ALL results (no filtering, no TM2 code search)
        Parameters parameters = new Parameters();
        parameters.setId("search-by-symptoms-all-results-" + System.currentTimeMillis());
        parameters.addParameter("result", new BooleanType(true));
        parameters.addParameter("totalMatches", new IntegerType(results.size()));
        parameters.addParameter("matchedSymptoms", new StringType(String.join(", ", symptoms)));

        // Add all results as parameter groups
        for (int i = 0; i < results.size(); i++) {
            NamasteCode namasteCode = results.get(i);

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

            // Add code description and symptom similarity score
            if (namasteCode.getNamasteDescription() != null) {
                matchGroup.addPart().setName("description").setValue(new StringType(namasteCode.getNamasteDescription()));
            }

            if (namasteCode.getConfidenceScore() != null) {
                matchGroup.addPart().setName("symptomSimilarityScore").setValue(new DecimalType(namasteCode.getConfidenceScore()));
            }

            parameters.addParameter(matchGroup);
        }

        return parameters;

        // ============================================================================
        // UNUSED CODE BELOW - KEPT FOR FUTURE REFERENCE (OLD FILTERING LOGIC)
        // ============================================================================
        /*
        // OLD LOGIC: Take the first result (most relevant symptom match)
        NamasteCode bestSymptomMatch = results.get(0);

        log.info("Best symptom match found: TM2 Code = {}, Symptom Similarity Score = {}", 
                bestSymptomMatch.getTm2Code(), bestSymptomMatch.getConfidenceScore());

        // Now search by the TM2 code of the best symptom match
        String tm2Code = bestSymptomMatch.getTm2Code();
        if (tm2Code == null || tm2Code.trim().isEmpty()) {
            // If no TM2 code available, return the original match as parameters
            Parameters parameters = new Parameters();
            parameters.setId("search-by-symptoms-fallback-" + System.currentTimeMillis());
            parameters.addParameter("result", new BooleanType(true));
            parameters.addParameter("message", new StringType("Found symptom match but no TM2 code for further search"));

            // Add the original match details
            Parameters.ParametersParameterComponent matchGroup = new Parameters.ParametersParameterComponent();
            matchGroup.setName("match");

            Parameters.ParametersParameterComponent codeParam = new Parameters.ParametersParameterComponent();
            codeParam.setName("code");
            codeParam.addPart().setName("system").setValue(new UriType("http://terminology.hl7.org.in/CodeSystem/namaste"));
            codeParam.addPart().setName("code").setValue(new CodeType(bestSymptomMatch.getNamasteCode()));
            codeParam.addPart().setName("display").setValue(new StringType(bestSymptomMatch.getNamasteName()));
            matchGroup.addPart(codeParam);

            // Add symptom similarity score
            if (bestSymptomMatch.getConfidenceScore() != null) {
                matchGroup.addPart().setName("symptomSimilarityScore").setValue(new DecimalType(bestSymptomMatch.getConfidenceScore()));
            }

            parameters.addParameter(matchGroup);
            return parameters;
        }

        // Search by the TM2 code to get comprehensive results
        Parameters codeSearchResults = createSearchByCodeResult(tm2Code);
        
        // Add the original symptom similarity score to the results for reference
        if (bestSymptomMatch.getConfidenceScore() != null) {
            codeSearchResults.addParameter("originalSymptomSimilarity", new DecimalType(bestSymptomMatch.getConfidenceScore()));
        }
        
        // Add the matched symptoms for reference
        codeSearchResults.addParameter("matchedSymptoms", new StringType(String.join(", ", symptoms)));
        
        return codeSearchResults;
        */
    }
}