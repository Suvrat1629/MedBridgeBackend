package com.namaste.Namaste.to.TM2.Controller;

import com.namaste.Namaste.to.TM2.Service.TerminologyFhirService;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * FHIR R4-COMPLIANT Controller for terminology requirements
 * ALL responses are proper FHIR resources with correct content-type
 */
@RestController
@RequestMapping("/api/fhir")
@CrossOrigin(origins = "*")
public class FhirBundleController {

    private static final Logger log = LoggerFactory.getLogger(FhirBundleController.class);
    private final TerminologyFhirService terminologyFhirService;

    // FHIR R4 Content Type
    private static final String FHIR_JSON_CONTENT_TYPE = "application/fhir+json;fhirVersion=4.0";

    public FhirBundleController(TerminologyFhirService terminologyFhirService) {
        this.terminologyFhirService = terminologyFhirService;
    }

    /**
     * MAIN FEATURE 1: FHIR-COMPLIANT Search by Code
     * Searches in both tm2_code and code fields, returns FHIR Parameters
     */
    @GetMapping(value = "/search/code/{codeValue}", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> searchByCode(@PathVariable String codeValue) {
        log.info("FHIR search by code: {}", codeValue);

        try {
            Parameters parameters = terminologyFhirService.createSearchByCodeResult(codeValue);
            addFhirMetadata(parameters);
            String fhirJson = terminologyFhirService.toJson(parameters);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error in FHIR code search", e);
            return createFhirErrorResponse("Code search failed", e.getMessage());
        }
    }

    /**
     * FHIR-COMPLIANT Search by TM2 Code Only
     * Searches only in tm2_code field, returns FHIR Parameters
     */
    @GetMapping(value = "/search/tm2code/{codeValue}", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> searchByTm2Code(@PathVariable String codeValue) {
        log.info("FHIR search by TM2 code: {}", codeValue);

        try {
            Parameters parameters = terminologyFhirService.createSearchByTm2CodeResult(codeValue);
            addFhirMetadata(parameters);
            String fhirJson = terminologyFhirService.toJson(parameters);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error in FHIR TM2 code search", e);
            return createFhirErrorResponse("TM2 code search failed", e.getMessage());
        }
    }

    /**
     * FHIR-COMPLIANT Search by Code Only
     * Searches only in code field, returns FHIR Parameters
     */
    @GetMapping(value = "/search/codeonly/{codeValue}", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> searchByCodeOnly(@PathVariable String codeValue) {
        log.info("FHIR search by code only: {}", codeValue);

        try {
            Parameters parameters = terminologyFhirService.createSearchByCodeOnlyResult(codeValue);
            addFhirMetadata(parameters);
            String fhirJson = terminologyFhirService.toJson(parameters);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error in FHIR code only search", e);
            return createFhirErrorResponse("Code only search failed", e.getMessage());
        }
    }

    /**
     * MAIN FEATURE 2: FHIR-COMPLIANT Search by Symptoms
     * Searches in both code_description and tm2_definition fields, returns FHIR Bundle
     */
    @GetMapping(value = "/search/symptoms", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> searchBySymptoms(@RequestParam String query) {
        log.info("FHIR search by symptoms: {}", query);

        try {
            Bundle bundle = terminologyFhirService.createSearchBySymptomsResult(query);
            String fhirJson = terminologyFhirService.toJson(bundle);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error in FHIR symptom search", e);
            return createFhirErrorResponse("Symptom search failed", e.getMessage());
        }
    }

    // Helper methods for FHIR compliance
    private ResponseEntity<String> createFhirResponse(String fhirJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(FHIR_JSON_CONTENT_TYPE));
        headers.add("X-FHIR-Version", "4.0.1");
        return ResponseEntity.ok().headers(headers).body(fhirJson);
    }

    private ResponseEntity<String> createFhirErrorResponse(String message, String details) {
        try {
            OperationOutcome errorOutcome = new OperationOutcome();
            errorOutcome.setId("error-" + System.currentTimeMillis());
            addFhirMetadata(errorOutcome);

            OperationOutcome.OperationOutcomeIssueComponent errorIssue =
                    new OperationOutcome.OperationOutcomeIssueComponent();
            errorIssue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
            errorIssue.setCode(OperationOutcome.IssueType.PROCESSING);
            errorIssue.setDiagnostics("Error: " + message + ". Details: " + details);
            errorOutcome.addIssue(errorIssue);

            String fhirJson = terminologyFhirService.toJson(errorOutcome);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(FHIR_JSON_CONTENT_TYPE));
            headers.add("X-FHIR-Version", "4.0.1");

            return ResponseEntity.badRequest().headers(headers).body(fhirJson);
        } catch (Exception e) {
            // Fallback to minimal FHIR error
            return ResponseEntity.badRequest()
                    .contentType(MediaType.valueOf(FHIR_JSON_CONTENT_TYPE))
                    .body("{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"processing\",\"diagnostics\":\"" + message + "\"}]}");
        }
    }

    private void addFhirMetadata(DomainResource resource) {
        Meta meta = new Meta();
        meta.setVersionId("1");
        meta.setLastUpdated(new Date());

        // Add profile for Indian EHR standards
        meta.addProfile("http://hl7.org.in/fhir/StructureDefinition/AyushTerminology");

        // Add security classification
        meta.addSecurity()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-Confidentiality")
                .setCode("N")
                .setDisplay("Normal");

        // Add terminology service tag
        meta.addTag()
                .setSystem("http://terminology.hl7.org.in/CodeSystem/terminology-tags")
                .setCode("terminology-service")
                .setDisplay("Terminology Service");

        resource.setMeta(meta);
    }

    private void addFhirMetadata(Parameters parameters) {
        Meta meta = new Meta();
        meta.setVersionId("1");
        meta.setLastUpdated(new Date());

        // Add profile for Indian EHR standards
        meta.addProfile("http://hl7.org.in/fhir/StructureDefinition/AyushParameters");

        // Add security classification
        meta.addSecurity()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-Confidentiality")
                .setCode("N")
                .setDisplay("Normal");

        // Add terminology service tag
        meta.addTag()
                .setSystem("http://terminology.hl7.org.in/CodeSystem/terminology-tags")
                .setCode("terminology-operation")
                .setDisplay("Terminology Operation");

        parameters.setMeta(meta);
    }
}