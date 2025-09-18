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
            matchGroup.setName("match" + (i + 1));

            // Add found code details
            Parameters.ParametersParameterComponent codeParam = new Parameters.ParametersParameterComponent();
            codeParam.setName("code");
            codeParam.addPart().setName("system").setValue(new UriType("http://terminology.hl7.org.in/CodeSystem/namaste"));
            codeParam.addPart().setName("code").setValue(new CodeType(namasteCode.getNamasteCode()));
            codeParam.addPart().setName("display").setValue(new StringType(namasteCode.getNamasteName()));
            matchGroup.addPart(codeParam);

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

            matchGroup.addPart().setName("type").setValue(new StringType(namasteCode.getNamasteCategory()));

            parameters.addParameter(matchGroup);
        }

        return parameters;
    }

    /**
     * Create FHIR Bundle for search by symptoms result - MAIN FEATURE 2
     */
    public Bundle createSearchBySymptomsResult(String symptomQuery) {
        log.info("Creating FHIR Bundle for symptom search: {}", symptomQuery);

        Bundle bundle = new Bundle();
        bundle.setId("search-by-symptoms-result-" + System.currentTimeMillis());
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTimestamp(new Date());

        // Search using the terminology service
        List<NamasteCode> results = terminologyService.searchBySymptoms(symptomQuery);

        bundle.setTotal(results.size());

        // Add each result as a Bundle entry with Parameters resource
        for (int i = 0; i < results.size(); i++) {
            NamasteCode namasteCode = results.get(i);

            Parameters entryParameters = new Parameters();
            entryParameters.setId("symptom-match-" + i);

            // Add code details
            Parameters.ParametersParameterComponent codeParam = new Parameters.ParametersParameterComponent();
            codeParam.setName("code");
            codeParam.addPart().setName("system").setValue(new UriType("http://terminology.hl7.org.in/CodeSystem/namaste"));
            codeParam.addPart().setName("code").setValue(new CodeType(namasteCode.getNamasteCode()));
            codeParam.addPart().setName("display").setValue(new StringType(namasteCode.getNamasteName()));
            entryParameters.addParameter(codeParam);

            // Add TM2 mapping if available
            if (namasteCode.getIcd11Tm2Code() != null) {
                Parameters.ParametersParameterComponent tm2Param = new Parameters.ParametersParameterComponent();
                tm2Param.setName("tm2Mapping");
                tm2Param.addPart().setName("system").setValue(new UriType("http://id.who.int/icd/release/11/tm2"));
                tm2Param.addPart().setName("code").setValue(new CodeType(namasteCode.getIcd11Tm2Code()));
                tm2Param.addPart().setName("display").setValue(new StringType(namasteCode.getIcd11Tm2Name()));
                tm2Param.addPart().setName("definition").setValue(new StringType(namasteCode.getIcd11Tm2Description()));
                tm2Param.addPart().setName("link").setValue(new UriType(namasteCode.getIcd11Tm2Uri()));
                entryParameters.addParameter(tm2Param);
            }

            // Add descriptions and metadata
            if (namasteCode.getNamasteDescription() != null) {
                entryParameters.addParameter("description", new StringType(namasteCode.getNamasteDescription()));
            }

            if (namasteCode.getConfidenceScore() != null) {
                entryParameters.addParameter("confidenceScore", new DecimalType(namasteCode.getConfidenceScore()));
            }

            entryParameters.addParameter("type", new StringType(namasteCode.getNamasteCategory()));

            // Add to bundle
            Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
            entry.setResource(entryParameters);
            entry.setFullUrl("Parameters/" + entryParameters.getId());
            bundle.addEntry(entry);
        }

        return bundle;
    }
}