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
     * Generate FHIR CodeSystem for NAMASTE terminology
     * This is what EMR systems need to integrate with
     */
    public CodeSystem generateNamasteCodeSystem() {
        log.info("Generating NAMASTE FHIR CodeSystem");

        CodeSystem codeSystem = new CodeSystem();
        codeSystem.setId("namaste");
        codeSystem.setUrl("http://terminology.hl7.org.in/CodeSystem/namaste");
        codeSystem.setVersion("1.0.0");
        codeSystem.setName("NAMASTE");
        codeSystem.setTitle("National AYUSH Morbidity & Standardized Terminologies Electronic");
        codeSystem.setStatus(Enumerations.PublicationStatus.ACTIVE);
        codeSystem.setExperimental(false);
        codeSystem.setDate(new Date());
        codeSystem.setPublisher("Ministry of AYUSH, Government of India");
        codeSystem.setDescription("Standardized terminology for Ayurveda, Siddha, and Unani disorders");
        codeSystem.setCaseSensitive(true);
        codeSystem.setContent(CodeSystem.CodeSystemContentMode.COMPLETE);

        // Add actual NAMASTE concepts from database
        List<NamasteCode> allCodes = terminologyService.getAllActiveCodes();
        codeSystem.setCount(allCodes.size());

        for (NamasteCode namasteCode : allCodes) {
            CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent();
            concept.setCode(namasteCode.getNamasteCode());
            concept.setDisplay(namasteCode.getNamasteName());
            concept.setDefinition(namasteCode.getNamasteDescription());

            // Add properties for ICD-11 mappings
            if (namasteCode.getIcd11Tm2Code() != null) {
                CodeSystem.ConceptPropertyComponent tm2Property = new CodeSystem.ConceptPropertyComponent();
                tm2Property.setCode("icd11-tm2");
                tm2Property.setValue(new StringType(namasteCode.getIcd11Tm2Code()));
                concept.addProperty(tm2Property);
            }

            if (namasteCode.getIcd11BiomedicineCode() != null) {
                CodeSystem.ConceptPropertyComponent biomedProperty = new CodeSystem.ConceptPropertyComponent();
                biomedProperty.setCode("icd11-biomedicine");
                biomedProperty.setValue(new StringType(namasteCode.getIcd11BiomedicineCode()));
                concept.addProperty(biomedProperty);
            }

            // Add category property
            CodeSystem.ConceptPropertyComponent categoryProperty = new CodeSystem.ConceptPropertyComponent();
            categoryProperty.setCode("category");
            categoryProperty.setValue(new StringType(namasteCode.getNamasteCategory()));
            concept.addProperty(categoryProperty);

            codeSystem.addConcept(concept);
        }

        // Add property definitions
        addCodeSystemProperties(codeSystem);

        log.info("Generated NAMASTE CodeSystem with {} concepts", allCodes.size());
        return codeSystem;
    }

    /**
     * Generate FHIR ConceptMap for NAMASTE → ICD-11 TM2 mapping
     * This shows the mapping relationships
     */
    public ConceptMap generateNamasteToTm2ConceptMap() {
        log.info("Generating NAMASTE → ICD-11 TM2 ConceptMap");

        ConceptMap conceptMap = new ConceptMap();
        conceptMap.setId("namaste-to-icd11-tm2");
        conceptMap.setUrl("http://terminology.hl7.org.in/ConceptMap/namaste-to-icd11-tm2");
        conceptMap.setVersion("1.0.0");
        conceptMap.setName("NAMASTEToICD11TM2");
        conceptMap.setTitle("NAMASTE to ICD-11 TM2 Concept Map");
        conceptMap.setStatus(Enumerations.PublicationStatus.ACTIVE);
        conceptMap.setDate(new Date());
        conceptMap.setPublisher("Ministry of AYUSH, Government of India");
        conceptMap.setDescription("Mapping from NAMASTE codes to ICD-11 Traditional Medicine Module 2 codes");

        // Source and target systems
        conceptMap.setSource(new UriType("http://terminology.hl7.org.in/CodeSystem/namaste"));
        conceptMap.setTarget(new UriType("http://id.who.int/icd/release/11/tm2"));

        // Create mapping group
        ConceptMap.ConceptMapGroupComponent group = new ConceptMap.ConceptMapGroupComponent();
        group.setSource("http://terminology.hl7.org.in/CodeSystem/namaste");
        group.setTarget("http://id.who.int/icd/release/11/tm2");

        // Add mappings from database
        List<NamasteCode> mappedCodes = terminologyService.getCodesWithTm2Mapping();
        for (NamasteCode namasteCode : mappedCodes) {
            ConceptMap.SourceElementComponent element = new ConceptMap.SourceElementComponent();
            element.setCode(namasteCode.getNamasteCode());
            element.setDisplay(namasteCode.getNamasteName());

            ConceptMap.TargetElementComponent target = new ConceptMap.TargetElementComponent();
            target.setCode(namasteCode.getIcd11Tm2Code());
            target.setDisplay(namasteCode.getIcd11Tm2Name());
            target.setEquivalence(Enumerations.ConceptMapEquivalence.EQUIVALENT);

            element.addTarget(target);
            group.addElement(element);
        }

        conceptMap.addGroup(group);

        log.info("Generated ConceptMap with {} mappings", mappedCodes.size());
        return conceptMap;
    }

    /**
     * Generate FHIR ConceptMap for NAMASTE → ICD-11 Biomedicine mapping
     */
    public ConceptMap generateNamasteToBiomedicineConceptMap() {
        log.info("Generating NAMASTE → ICD-11 Biomedicine ConceptMap");

        ConceptMap conceptMap = new ConceptMap();
        conceptMap.setId("namaste-to-icd11-biomedicine");
        conceptMap.setUrl("http://terminology.hl7.org.in/ConceptMap/namaste-to-icd11-biomedicine");
        conceptMap.setVersion("1.0.0");
        conceptMap.setName("NAMASTEToICD11Biomedicine");
        conceptMap.setTitle("NAMASTE to ICD-11 Biomedicine Concept Map");
        conceptMap.setStatus(Enumerations.PublicationStatus.ACTIVE);
        conceptMap.setDate(new Date());
        conceptMap.setPublisher("Ministry of AYUSH, Government of India");
        conceptMap.setDescription("Mapping from NAMASTE codes to ICD-11 Biomedicine codes for insurance claims");

        conceptMap.setSource(new UriType("http://terminology.hl7.org.in/CodeSystem/namaste"));
        conceptMap.setTarget(new UriType("http://id.who.int/icd/release/11/biomedicine"));

        ConceptMap.ConceptMapGroupComponent group = new ConceptMap.ConceptMapGroupComponent();
        group.setSource("http://terminology.hl7.org.in/CodeSystem/namaste");
        group.setTarget("http://id.who.int/icd/release/11/biomedicine");

        List<NamasteCode> mappedCodes = terminologyService.getCodesWithBiomedicineMapping();
        for (NamasteCode namasteCode : mappedCodes) {
            ConceptMap.SourceElementComponent element = new ConceptMap.SourceElementComponent();
            element.setCode(namasteCode.getNamasteCode());
            element.setDisplay(namasteCode.getNamasteName());

            ConceptMap.TargetElementComponent target = new ConceptMap.TargetElementComponent();
            target.setCode(namasteCode.getIcd11BiomedicineCode());
            target.setDisplay(namasteCode.getIcd11BiomedicineName());
            target.setEquivalence(Enumerations.ConceptMapEquivalence.EQUIVALENT);

            element.addTarget(target);
            group.addElement(element);
        }

        conceptMap.addGroup(group);

        log.info("Generated Biomedicine ConceptMap with {} mappings", mappedCodes.size());
        return conceptMap;
    }

    /**
     * Create a CodeableConcept with dual coding (Enhanced for any disease name)
     * Input: Any disease name → Output: FHIR CodeableConcept with all mappings
     */
    public CodeableConcept createDualCodedConcept(String diseaseName) {
        log.info("Creating dual-coded concept for disease: {}", diseaseName);

        NamasteCode code = null;

        // Try multiple search strategies

        // 1. Exact NAMASTE name match
        var exactMatch = terminologyService.getByNamasteName(diseaseName);
        if (exactMatch.isPresent()) {
            code = exactMatch.get();
            log.info("Found exact NAMASTE name match for: {}", diseaseName);
        }

        // 2. If not found, try comprehensive search (partial matching)
        if (code == null) {
            List<NamasteCode> searchResults = terminologyService.comprehensiveSearch(diseaseName);
            if (!searchResults.isEmpty()) {
                code = searchResults.get(0); // Take first match
                log.info("Found comprehensive search match for: {}", diseaseName);
            }
        }

        // 3. If still not found, try reverse lookup from ICD-11 TM2 code
        if (code == null) {
            var tm2Match = terminologyService.findByIcd11Tm2Code(diseaseName);
            if (tm2Match.isPresent()) {
                code = tm2Match.get();
                log.info("Found ICD-11 TM2 code match for: {}", diseaseName);
            }
        }

        // 4. If still not found, try reverse lookup from ICD-11 Biomedicine code
        if (code == null) {
            var biomedicineMatch = terminologyService.findByIcd11BiomedicineCode(diseaseName);
            if (biomedicineMatch.isPresent()) {
                code = biomedicineMatch.get();
                log.info("Found ICD-11 Biomedicine code match for: {}", diseaseName);
            }
        }

        // 5. If still not found, try searching by ICD-11 names
        if (code == null) {
            code = findByIcd11Names(diseaseName);
            if (code != null) {
                log.info("Found ICD-11 name match for: {}", diseaseName);
            }
        }

        if (code == null) {
            String suggestions = getSuggestions(diseaseName);
            String errorMessage = "Disease not found: " + diseaseName;
            if (!suggestions.isEmpty()) {
                errorMessage += ". Try: " + suggestions;
            }
            throw new RuntimeException(errorMessage);
        }

        // Build the CodeableConcept with all available codings
        CodeableConcept concept = new CodeableConcept();

        // Primary NAMASTE coding
        Coding namasteCoding = new Coding();
        namasteCoding.setSystem("http://terminology.hl7.org.in/CodeSystem/namaste");
        namasteCoding.setCode(code.getNamasteCode());
        namasteCoding.setDisplay(code.getNamasteName());
        namasteCoding.setUserSelected(true);
        concept.addCoding(namasteCoding);

        // ICD-11 TM2 coding
        if (code.getIcd11Tm2Code() != null) {
            Coding tm2Coding = new Coding();
            tm2Coding.setSystem("http://id.who.int/icd/release/11/tm2");
            tm2Coding.setCode(code.getIcd11Tm2Code());
            tm2Coding.setDisplay(code.getIcd11Tm2Name());
            concept.addCoding(tm2Coding);
        }

        // ICD-11 Biomedicine coding
        if (code.getIcd11BiomedicineCode() != null) {
            Coding biomedCoding = new Coding();
            biomedCoding.setSystem("http://id.who.int/icd/release/11/biomedicine");
            biomedCoding.setCode(code.getIcd11BiomedicineCode());
            biomedCoding.setDisplay(code.getIcd11BiomedicineName());
            concept.addCoding(biomedCoding);
        }

        concept.setText(code.getNamasteName());
        log.info("Successfully created dual-coded concept for: {} -> {}", diseaseName, code.getNamasteName());
        return concept;
    }

    /**
     * Helper method to find by ICD-11 names (case-insensitive partial matching)
     */
    private NamasteCode findByIcd11Names(String diseaseName) {
        List<NamasteCode> allCodes = terminologyService.getAllActiveCodes();

        String searchTerm = diseaseName.toLowerCase().trim();

        for (NamasteCode namasteCode : allCodes) {
            // Check ICD-11 TM2 name
            if (namasteCode.getIcd11Tm2Name() != null &&
                    namasteCode.getIcd11Tm2Name().toLowerCase().contains(searchTerm)) {
                return namasteCode;
            }

            // Check ICD-11 Biomedicine name
            if (namasteCode.getIcd11BiomedicineName() != null &&
                    namasteCode.getIcd11BiomedicineName().toLowerCase().contains(searchTerm)) {
                return namasteCode;
            }

            // Check NAMASTE description
            if (namasteCode.getNamasteDescription() != null &&
                    namasteCode.getNamasteDescription().toLowerCase().contains(searchTerm)) {
                return namasteCode;
            }
        }

        return null;
    }

    /**
     * Helper method to provide suggestions when disease is not found
     */
    private String getSuggestions(String diseaseName) {
        try {
            List<NamasteCode> suggestions = terminologyService.searchForAutoComplete(diseaseName, 3);
            return suggestions.stream()
                    .map(NamasteCode::getNamasteName)
                    .collect(java.util.stream.Collectors.joining(", "));
        } catch (Exception e) {
            log.warn("Error getting suggestions for: {}", diseaseName, e);
            return "";
        }
    }

    /**
     * Serialize FHIR resource to JSON
     */
    public String toJson(Resource resource) {
        return jsonParser.encodeResourceToString(resource);
    }

    /**
     * Serialize FHIR CodeableConcept to JSON
     */
    public String codeableConceptToJson(CodeableConcept concept) {
        return jsonParser.encodeToString(concept);
    }

    /**
     * Parse JSON to FHIR resource
     */
    public <T extends Resource> T fromJson(String json, Class<T> resourceType) {
        return jsonParser.parseResource(resourceType, json);
    }

    /**
     * Create expanded ValueSet for auto-complete functionality
     */
    public ValueSet createExpandedValueSet(String filter, int count) {
        log.info("Creating expanded ValueSet with filter: {} and count: {}", filter, count);

        ValueSet valueSet = new ValueSet();
        valueSet.setId("namaste-expansion");
        valueSet.setUrl("http://terminology.hl7.org.in/ValueSet/namaste");
        valueSet.setName("NAMASTEValueSet");
        valueSet.setTitle("NAMASTE Value Set");
        valueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);
        valueSet.setDate(new Date());
        valueSet.setPublisher("Ministry of AYUSH, Government of India");
        valueSet.setDescription("Value set for NAMASTE terminology auto-complete");

        // Create expansion
        ValueSet.ValueSetExpansionComponent expansion = new ValueSet.ValueSetExpansionComponent();
        expansion.setTimestamp(new Date());

        // Get matching codes from terminology service
        List<NamasteCode> matchingCodes;
        if (filter != null && !filter.trim().isEmpty()) {
            matchingCodes = terminologyService.searchForAutoComplete(filter.trim(), count);
        } else {
            // Get first 'count' codes if no filter provided
            matchingCodes = terminologyService.getAllActiveCodes()
                    .stream()
                    .limit(count)
                    .collect(java.util.stream.Collectors.toList());
        }

        expansion.setTotal(matchingCodes.size());

        // Add matching concepts to expansion
        for (NamasteCode namasteCode : matchingCodes) {
            ValueSet.ValueSetExpansionContainsComponent contains = new ValueSet.ValueSetExpansionContainsComponent();
            contains.setSystem("http://terminology.hl7.org.in/CodeSystem/namaste");
            contains.setCode(namasteCode.getNamasteCode());
            contains.setDisplay(namasteCode.getNamasteName());
            expansion.addContains(contains);
        }

        valueSet.setExpansion(expansion);
        return valueSet;
    }

    /**
     * Create FHIR Parameters for translate operation result
     */
    public Parameters createTranslateResult(String code, String system) {
        log.info("Creating translate result for code: {} from system: {}", code, system);

        Parameters parameters = new Parameters();
        parameters.setId("translate-result-" + code);

        // Find the code in our terminology
        var namasteData = terminologyService.getByNamasteCode(code);

        if (namasteData.isPresent()) {
            NamasteCode namasteCode = namasteData.get();

            // Result parameter (true = translation found)
            parameters.addParameter("result", new BooleanType(true));

            // Match parameter with translation details
            Parameters.ParametersParameterComponent matchParam = new Parameters.ParametersParameterComponent();
            matchParam.setName("match");

            // Add equivalence
            matchParam.addPart().setName("equivalence").setValue(new CodeType("equivalent"));

            // Add target concept (ICD-11 TM2)
            if (namasteCode.getIcd11Tm2Code() != null) {
                Parameters.ParametersParameterComponent conceptParam = new Parameters.ParametersParameterComponent();
                conceptParam.setName("concept");
                conceptParam.addPart().setName("system").setValue(new UriType("http://id.who.int/icd/release/11/tm2"));
                conceptParam.addPart().setName("code").setValue(new CodeType(namasteCode.getIcd11Tm2Code()));
                conceptParam.addPart().setName("display").setValue(new StringType(namasteCode.getIcd11Tm2Name()));
                matchParam.addPart(conceptParam);
            }

            parameters.addParameter(matchParam);
        } else {
            // Result parameter (false = no translation found)
            parameters.addParameter("result", new BooleanType(false));
            parameters.addParameter("message", new StringType("No mapping found for code: " + code));
        }

        return parameters;
    }

    /**
     * Create FHIR Parameters for lookup operation result
     */
    public Parameters createLookupResult(String code, String system) {
        log.info("Creating lookup result for code: {} from system: {}", code, system);

        Parameters parameters = new Parameters();
        parameters.setId("lookup-result-" + code);

        // Find the code in our terminology
        var namasteData = terminologyService.getByNamasteCode(code);

        if (namasteData.isPresent()) {
            NamasteCode namasteCode = namasteData.get();

            // Name parameter (CodeSystem name)
            parameters.addParameter("name", new StringType("NAMASTE"));

            // Display parameter
            parameters.addParameter("display", new StringType(namasteCode.getNamasteName()));

            // Definition parameter
            if (namasteCode.getNamasteDescription() != null) {
                parameters.addParameter("definition", new StringType(namasteCode.getNamasteDescription()));
            }

            // Property parameters for ICD-11 mappings
            if (namasteCode.getIcd11Tm2Code() != null) {
                Parameters.ParametersParameterComponent tm2Property = new Parameters.ParametersParameterComponent();
                tm2Property.setName("property");
                tm2Property.addPart().setName("code").setValue(new CodeType("icd11-tm2"));
                tm2Property.addPart().setName("value").setValue(new StringType(namasteCode.getIcd11Tm2Code()));
                tm2Property.addPart().setName("description").setValue(new StringType(namasteCode.getIcd11Tm2Name()));
                parameters.addParameter(tm2Property);
            }

            if (namasteCode.getIcd11BiomedicineCode() != null) {
                Parameters.ParametersParameterComponent biomedProperty = new Parameters.ParametersParameterComponent();
                biomedProperty.setName("property");
                biomedProperty.addPart().setName("code").setValue(new CodeType("icd11-biomedicine"));
                biomedProperty.addPart().setName("value").setValue(new StringType(namasteCode.getIcd11BiomedicineCode()));
                biomedProperty.addPart().setName("description").setValue(new StringType(namasteCode.getIcd11BiomedicineName()));
                parameters.addParameter(biomedProperty);
            }

            // Category property
            Parameters.ParametersParameterComponent categoryProperty = new Parameters.ParametersParameterComponent();
            categoryProperty.setName("property");
            categoryProperty.addPart().setName("code").setValue(new CodeType("category"));
            categoryProperty.addPart().setName("value").setValue(new StringType(namasteCode.getNamasteCategory()));
            parameters.addParameter(categoryProperty);

        } else {
            // Add error parameter
            parameters.addParameter("error", new StringType("Code not found: " + code));
        }

        return parameters;
    }

    // Helper method to add property definitions to CodeSystem
    private void addCodeSystemProperties(CodeSystem codeSystem) {
        // Category property
        CodeSystem.PropertyComponent categoryProperty = new CodeSystem.PropertyComponent();
        categoryProperty.setCode("category");
        categoryProperty.setType(CodeSystem.PropertyType.STRING);
        categoryProperty.setDescription("Traditional medicine category (Ayurveda, Siddha, Unani)");
        codeSystem.addProperty(categoryProperty);

        // ICD-11 TM2 property
        CodeSystem.PropertyComponent tm2Property = new CodeSystem.PropertyComponent();
        tm2Property.setCode("icd11-tm2");
        tm2Property.setType(CodeSystem.PropertyType.CODE);
        tm2Property.setDescription("Mapped ICD-11 TM2 code");
        codeSystem.addProperty(tm2Property);

        // ICD-11 Biomedicine property
        CodeSystem.PropertyComponent biomedProperty = new CodeSystem.PropertyComponent();
        biomedProperty.setCode("icd11-biomedicine");
        biomedProperty.setType(CodeSystem.PropertyType.CODE);
        biomedProperty.setDescription("Mapped ICD-11 Biomedicine code");
        codeSystem.addProperty(biomedProperty);
    }
}