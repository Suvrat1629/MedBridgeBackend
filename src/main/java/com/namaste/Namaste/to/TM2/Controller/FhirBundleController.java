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
     * FHIR-COMPLIANT: Get FHIR CodeSystem for NAMASTE
     */
    @GetMapping(value = "/CodeSystem/namaste", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> getNamasteCodeSystem() {
        log.info("Generating FHIR-compliant NAMASTE CodeSystem");

        try {
            CodeSystem codeSystem = terminologyFhirService.generateNamasteCodeSystem();
            addFhirMetadata(codeSystem);
            String fhirJson = terminologyFhirService.toJson(codeSystem);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error generating FHIR CodeSystem", e);
            return createFhirErrorResponse("Failed to generate FHIR CodeSystem", e.getMessage());
        }
    }

    /**
     * FHIR-COMPLIANT: Get FHIR ConceptMap for NAMASTE → ICD-11 TM2
     */
    @GetMapping(value = "/ConceptMap/namaste-to-tm2", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> getNamasteToTm2ConceptMap() {
        log.info("Generating FHIR-compliant NAMASTE → ICD-11 TM2 ConceptMap");

        try {
            ConceptMap conceptMap = terminologyFhirService.generateNamasteToTm2ConceptMap();
            addFhirMetadata(conceptMap);
            String fhirJson = terminologyFhirService.toJson(conceptMap);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error generating TM2 ConceptMap", e);
            return createFhirErrorResponse("Failed to generate TM2 ConceptMap", e.getMessage());
        }
    }

    /**
     * FHIR-COMPLIANT: Get FHIR ConceptMap for NAMASTE → ICD-11 Biomedicine
     */
    @GetMapping(value = "/ConceptMap/namaste-to-biomedicine", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> getNamasteToBiomedicineConceptMap() {
        log.info("Generating FHIR-compliant NAMASTE → ICD-11 Biomedicine ConceptMap");

        try {
            ConceptMap conceptMap = terminologyFhirService.generateNamasteToBiomedicineConceptMap();
            addFhirMetadata(conceptMap);
            String fhirJson = terminologyFhirService.toJson(conceptMap);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error generating Biomedicine ConceptMap", e);
            return createFhirErrorResponse("Failed to generate Biomedicine ConceptMap", e.getMessage());
        }
    }

    /**
     * FHIR-COMPLIANT: FHIR Parameters operation for dual coding
     * Returns FHIR Parameters resource instead of raw CodeableConcept
     */
    @PostMapping(value = "/Parameters/$code-disease", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> createDualCodedParameters(@RequestBody DiseaseRequest request) {
        log.info("Creating FHIR-compliant dual-coded Parameters for disease: {}", request.getDiseaseName());

        try {
            CodeableConcept concept = terminologyFhirService.createDualCodedConcept(request.getDiseaseName());

            // Wrap in FHIR Parameters resource (FHIR-compliant)
            Parameters parameters = new Parameters();
            parameters.setId("dual-coding-result");
            addFhirMetadata(parameters);

            Parameters.ParametersParameterComponent param = new Parameters.ParametersParameterComponent();
            param.setName("codeableConcept");
            param.setValue(concept);
            parameters.addParameter(param);

            // Add input parameter for reference
            Parameters.ParametersParameterComponent inputParam = new Parameters.ParametersParameterComponent();
            inputParam.setName("diseaseName");
            inputParam.setValue(new StringType(request.getDiseaseName()));
            parameters.addParameter(inputParam);

            String fhirJson = terminologyFhirService.toJson(parameters);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error creating dual-coded Parameters for disease: {}", request.getDiseaseName(), e);
            return createFhirErrorResponse("Disease not found or mapping failed", e.getMessage());
        }
    }

    /**
     * FHIR-COMPLIANT: Bundle upload returns FHIR OperationOutcome
     */
//    @PostMapping(value = "/Bundle", produces = FHIR_JSON_CONTENT_TYPE)
//    public ResponseEntity<String> uploadBundle(@RequestBody String bundleJson) {
//        log.info("Processing FHIR Bundle for terminology extraction");
//
//        try {
//            Bundle bundle = terminologyFhirService.fromJson(bundleJson, Bundle.class);
//            BundleAnalysisResult result = analyzeBundleTerminology(bundle);
//
//            // Create FHIR-compliant OperationOutcome
//            OperationOutcome operationOutcome = new OperationOutcome();
//            operationOutcome.setId("bundle-processing-result");
//            addFhirMetadata(operationOutcome);
//
//            // Add success issue
//            OperationOutcome.OperationOutcomeIssueComponent successIssue =
//                    new OperationOutcome.OperationOutcomeIssueComponent();
//            successIssue.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
//
//            successIssue.setDiagnostics(String.format("Bundle processed successfully. Extracted %d NAMASTE codes, %d ICD-11 TM2 codes, %d ICD-11 Biomedicine codes",
//                    result.getNamasteCodesCount(), result.getIcd11Tm2CodesCount(), result.getIcd11BiomedicineCodesCount()));
//            operationOutcome.addIssue(successIssue);
//
//            if (result.isValidMappings()) {
//                OperationOutcome.OperationOutcomeIssueComponent validationIssue =
//                        new OperationOutcome.OperationOutcomeIssueComponent();
//                validationIssue.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
//                validationIssue.setCode(OperationOutcome.IssueType.INFORMATIONAL);
//                validationIssue.setDiagnostics("Bundle contains valid dual coding with NAMASTE and ICD-11 mappings");
//                operationOutcome.addIssue(validationIssue);
//            }
//
//            String fhirJson = terminologyFhirService.toJson(operationOutcome);
//            return createFhirResponse(fhirJson);
//        } catch (Exception e) {
//            log.error("Error processing FHIR Bundle", e);
//            return createFhirErrorResponse("Failed to process FHIR Bundle", e.getMessage());
//        }
//    }

    /**
     * FHIR-COMPLIANT: ValueSet expansion for auto-complete
     */
    @GetMapping(value = "/ValueSet/namaste/$expand", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> expandValueSet(
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "10") int count) {

        log.info("FHIR ValueSet expansion with filter: {} and count: {}", filter, count);

        try {
            ValueSet valueSet = terminologyFhirService.createExpandedValueSet(filter, count);
            addFhirMetadata(valueSet);
            String fhirJson = terminologyFhirService.toJson(valueSet);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error creating FHIR ValueSet expansion", e);
            return createFhirErrorResponse("Failed to expand ValueSet", e.getMessage());
        }
    }

    /**
     * FHIR-COMPLIANT: ConceptMap translate operation returns FHIR Parameters
     */
//    @GetMapping(value = "/ConceptMap/namaste-to-tm2/$translate", produces = FHIR_JSON_CONTENT_TYPE)
//    public ResponseEntity<String> translateCode(
//            @RequestParam String code,
//            @RequestParam(defaultValue = "http://terminology.hl7.org.in/CodeSystem/namaste") String system) {
//
//        log.info("FHIR ConceptMap translate: {} from system: {}", code, system);
//
//        try {
//            Parameters parameters = terminologyFhirService.createTranslateResult(code, system);
//            addFhirMetadata(parameters);
//            String fhirJson = terminologyFhirService.toJson(parameters);
//            return createFhirResponse(fhirJson);
//        } catch (Exception e) {
//            log.error("Error translating code", e);
//            return createFhirErrorResponse("Translation failed", e.getMessage());
//        }
//    }

    /**
     * FHIR-COMPLIANT: CodeSystem lookup operation returns FHIR Parameters
     */
    @GetMapping(value = "/CodeSystem/namaste/$lookup", produces = FHIR_JSON_CONTENT_TYPE)
    public ResponseEntity<String> lookupCode(
            @RequestParam String code,
            @RequestParam(defaultValue = "http://terminology.hl7.org.in/CodeSystem/namaste") String system) {

        log.info("FHIR CodeSystem lookup: {} from system: {}", code, system);

        try {
            Parameters parameters = terminologyFhirService.createLookupResult(code, system);
            addFhirMetadata(parameters);
            String fhirJson = terminologyFhirService.toJson(parameters);
            return createFhirResponse(fhirJson);
        } catch (Exception e) {
            log.error("Error looking up code", e);
            return createFhirErrorResponse("Lookup failed", e.getMessage());
        }
    }

    /**
     * FHIR-COMPLIANT: Health check returns FHIR CapabilityStatement
     */
//    @GetMapping(value = "/metadata", produces = FHIR_JSON_CONTENT_TYPE)
//    public ResponseEntity<String> getCapabilityStatement() {
//        log.info("Generating FHIR CapabilityStatement");
//
//        try {
//            CapabilityStatement capabilityStatement = createCapabilityStatement();
//            addFhirMetadata(capabilityStatement);
//            String fhirJson = terminologyFhirService.toJson(capabilityStatement);
//            return createFhirResponse(fhirJson);
//        } catch (Exception e) {
//            log.error("Error generating CapabilityStatement", e);
//            return createFhirErrorResponse("Failed to generate CapabilityStatement", e.getMessage());
//        }
//    }

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

//    private CapabilityStatement createCapabilityStatement() {
//        CapabilityStatement capability = new CapabilityStatement();
//        capability.setId("namaste-terminology-service");
//        capability.setUrl("http://terminology.hl7.org.in/fhir/CapabilityStatement/namaste-terminology-service");
//        capability.setVersion("1.0.0");
//        capability.setName("NAMASTETerminologyService");
//        capability.setTitle("NAMASTE FHIR Terminology Service");
//        capability.setStatus(Enumerations.PublicationStatus.ACTIVE);
//        capability.setDate(new Date());
//        capability.setPublisher("Ministry of AYUSH, Government of India");
//        capability.setDescription("FHIR R4 compliant terminology service for NAMASTE codes with ICD-11 mappings");
//        capability.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);
//        capability.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
//        capability.addFormat("application/fhir+json");
//
//        // Software component
//        CapabilityStatement.CapabilityStatementSoftwareComponent software =
//                new CapabilityStatement.CapabilityStatementSoftwareComponent();
//        software.setName("NAMASTE-TM2 FHIR Terminology Service");
//        software.setVersion("1.0.0");
//        capability.setSoftware(software);
//
//        // Implementation component
//        CapabilityStatement.CapabilityStatementImplementationComponent implementation =
//                new CapabilityStatement.CapabilityStatementImplementationComponent();
//        implementation.setDescription("Disease name to ICD-11 codes mapping with FHIR compliance");
//        implementation.setUrl("http://localhost:8082/api/fhir");
//        capability.setImplementation(implementation);
//
//        // REST component
//        CapabilityStatement.CapabilityStatementRestComponent rest =
//                new CapabilityStatement.CapabilityStatementRestComponent();
//        rest.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);
//
//        // Add CodeSystem resource
//        CapabilityStatement.CapabilityStatementRestResourceComponent codeSystemResource =
//                new CapabilityStatement.CapabilityStatementRestResourceComponent();
//        codeSystemResource.setType("CodeSystem");
//        codeSystemResource.addInteraction()
//                .setCode(CapabilityStatement.TypeRestfulInteraction.READ);
//        codeSystemResource.addOperation()
//                .setName("lookup")
//                .setDefinition("http://hl7.org/fhir/OperationDefinition/CodeSystem-lookup");
//        rest.addResource(codeSystemResource);
//
//        // Add ConceptMap resource
//        CapabilityStatement.CapabilityStatementRestResourceComponent conceptMapResource =
//                new CapabilityStatement.CapabilityStatementRestResourceComponent();
//        conceptMapResource.setType("ConceptMap");
//        conceptMapResource.addInteraction()
//                .setCode(CapabilityStatement.TypeRestfulInteraction.READ);
//        conceptMapResource.addOperation()
//                .setName("translate")
//                .setDefinition("http://hl7.org/fhir/OperationDefinition/ConceptMap-translate");
//        rest.addResource(conceptMapResource);
//
//        // Add ValueSet resource
//        CapabilityStatement.CapabilityStatementRestResourceComponent valueSetResource =
//                new CapabilityStatement.CapabilityStatementRestResourceComponent();
//        valueSetResource.setType("ValueSet");
//        valueSetResource.addInteraction()
//                .setCode(CapabilityStatement.TypeRestfulInteraction.READ);
//        valueSetResource.addOperation()
//                .setName("expand")
//                .setDefinition("http://hl7.org/fhir/OperationDefinition/ValueSet-expand");
//        rest.addResource(valueSetResource);
//
//        capability.addRest(rest);
//        return capability;
//    }

//    private BundleAnalysisResult analyzeBundleTerminology(Bundle bundle) {
//        BundleAnalysisResult result = new BundleAnalysisResult();
//
//        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
//            if (entry.getResource() instanceof Condition) {
//                Condition condition = (Condition) entry.getResource();
//
//                if (condition.hasCode()) {
//                    for (Coding coding : condition.getCode().getCoding()) {
//                        String system = coding.getSystem();
//                        String code = coding.getCode();
//
//                        if ("http://terminology.hl7.org.in/CodeSystem/namaste".equals(system)) {
//                            result.addNamasteCode(code);
//                        } else if ("http://id.who.int/icd/release/11/tm2".equals(system)) {
//                            result.addIcd11Tm2Code(code);
//                        } else if ("http://id.who.int/icd/release/11/biomedicine".equals(system)) {
//                            result.addIcd11BiomedicineCode(code);
//                        }
//                    }
//                }
//            }
//        }
//
//        return result;
//    }

    // DTOs
    public static class DiseaseRequest {
        private String diseaseName;

        public String getDiseaseName() {
            return diseaseName;
        }

        public void setDiseaseName(String diseaseName) {
            this.diseaseName = diseaseName;
        }
    }

    public static class BundleAnalysisResult {
        private final java.util.Set<String> namasteCodes = new java.util.HashSet<>();
        private final java.util.Set<String> icd11Tm2Codes = new java.util.HashSet<>();
        private final java.util.Set<String> icd11BiomedicineCodes = new java.util.HashSet<>();

        public void addNamasteCode(String code) {
            namasteCodes.add(code);
        }

        public void addIcd11Tm2Code(String code) {
            icd11Tm2Codes.add(code);
        }

        public void addIcd11BiomedicineCode(String code) {
            icd11BiomedicineCodes.add(code);
        }

        public int getNamasteCodesCount() {
            return namasteCodes.size();
        }

        public int getIcd11Tm2CodesCount() {
            return icd11Tm2Codes.size();
        }

        public int getIcd11BiomedicineCodesCount() {
            return icd11BiomedicineCodes.size();
        }

        public boolean isValidMappings() {
            return !namasteCodes.isEmpty() && (!icd11Tm2Codes.isEmpty() || !icd11BiomedicineCodes.isEmpty());
        }
    }
}