package com.namaste.Namaste.to.TM2.Service;

import com.namaste.Namaste.to.TM2.Model.NamasteCode;
import com.namaste.Namaste.to.TM2.Repository.NamasteCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NamasteTerminologyService {

    private static final Logger log = LoggerFactory.getLogger(NamasteTerminologyService.class);
    private final NamasteCodeRepository namasteCodeRepository;

    public NamasteTerminologyService(NamasteCodeRepository namasteCodeRepository) {
        this.namasteCodeRepository = namasteCodeRepository;
    }

    /**
     * Search by code - Main feature 1
     * Searches in both tm2_code and code fields (EXACT MATCH ONLY)
     */
    public List<NamasteCode> searchByCode(String codeValue) {
        log.info("=== SEARCH BY CODE DEBUG START ===");
        log.info("Input codeValue: '{}'", codeValue);
        log.info("Input codeValue length: {}", codeValue != null ? codeValue.length() : "NULL");

        if (codeValue == null || codeValue.trim().isEmpty()) {
            log.warn("Code value is null or empty, returning empty result");
            return List.of();
        }

        String trimmedCode = codeValue.trim();
        log.info("Trimmed codeValue: '{}'", trimmedCode);
        log.info("Trimmed codeValue length: {}", trimmedCode.length());

        log.info("Calling repository.findByAnyCode with parameter: '{}'", trimmedCode);

        try {
            Optional<NamasteCode> tm2docu = namasteCodeRepository.findTopByCodeOrderByConfidenceScoreDesc(trimmedCode);
            if(tm2docu.isPresent())
                trimmedCode = tm2docu.get().getTm2Code().trim();
            Optional<List<NamasteCode>> result = namasteCodeRepository.findByTm2CodeOnly(trimmedCode);

            log.info("Repository call completed");

            if (result.isEmpty() || result.get().isEmpty()) {
                log.warn("No results found for code: '{}'", trimmedCode);
                return List.of();
            }

            List<NamasteCode> results = result.get();
            log.info("Results found: {}", results.size());

            // Filter results to only include codes with confidence score > 0.6 and limit to 6
            List<NamasteCode> filteredResults = results.stream()
                    .filter(code -> code.getConfidenceScore() != null && code.getConfidenceScore() > 0.6)
                    .collect(Collectors.toList());
            HashMap<String,NamasteCode> finalCodes = new HashMap<>();
            if(tm2docu.isPresent())
                finalCodes.put(tm2docu.get().getType(),tm2docu.get());
            for(NamasteCode code : filteredResults){
                if(!finalCodes.containsKey(code.getType()))
                    finalCodes.put(code.getType(),code);
                else {
                    if(tm2docu.isPresent()&& Objects.equals(code.getType(), tm2docu.get().getType()))
                        continue;
                    if(finalCodes.get(code.getType()).getConfidenceScore()<code.getConfidenceScore())
                        finalCodes.put(code.getType(),code);
                }
            }
            filteredResults = new ArrayList<>(finalCodes.values());
            log.info("Results after confidence filter (>0.6): {}", filteredResults.size());

            for (int i = 0; i < filteredResults.size(); i++) {
                NamasteCode match = filteredResults.get(i);
                log.info("Match {}: ID={}, tm2_code='{}', code='{}', code_title='{}', confidence={}",
                        i + 1, match.getId(), match.getTm2Code(), match.getCode(), match.getCodeTitle(), match.getConfidenceScore());
            }

            log.info("=== SEARCH BY CODE DEBUG END ===");
            return filteredResults;

        } catch (Exception e) {
            log.error("Exception occurred while searching for code: '{}'", trimmedCode, e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception message: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Search by symptoms/description - Main feature 2
     * Searches in both code_description and tm2_definition fields with fuzzy matching
     * Returns results ordered by text similarity to the query
     * Now supports multiple symptoms - documents must match ALL provided symptoms
     * Enhanced to return all three traditional medicine mappings for each matched disease
     * Results are grouped by TM2 code so frontend knows which mappings belong together
     */
    public List<DiseaseMapping> searchBySymptomsGrouped(List<String> symptoms) {
        log.info("Searching by symptoms/description (grouped): {}", symptoms);

        if (symptoms == null || symptoms.isEmpty()) {
            return List.of(); // Return empty list if no symptoms provided
        }

        // Filter out very short symptoms
        List<String> validSymptoms = symptoms.stream()
                .filter(symptom -> symptom != null && symptom.trim().length() >= 2)
                .map(String::trim)
                .collect(Collectors.toList());

        if (validSymptoms.isEmpty()) {
            return List.of();
        }

        log.info("Valid symptoms for search: {}", validSymptoms);

        // Use the optimized repository method for AND logic
        List<NamasteCode> results = namasteCodeRepository.findByAllSymptoms(validSymptoms);

        log.info("Repository AND query returned {} documents (documents matching ALL symptoms)", results.size());

        if (results.isEmpty()) {
            return List.of();
        }
        // Calculate multi-symptom similarity scores and sort by relevance
        List<NamasteCode> scoredResults = results.stream()
                .filter(code -> code.getConfidenceScore() > 0.6) // Only keep codes with some relevance
                .collect(Collectors.toList());

        // Now for each matched document, get all three traditional medicine mappings using searchByCode
        List<DiseaseMapping> groupedResults = new ArrayList<>();
        HashSet<String> processedTm2Codes = new HashSet<>(); // To avoid duplicate processing
        for (NamasteCode matchedCode : scoredResults) {
            String tm2Code = matchedCode.getTm2Code();

            // Skip if we've already processed this TM2 code
            if (tm2Code != null && !processedTm2Codes.contains(tm2Code)) {
                processedTm2Codes.add(tm2Code);

                log.info("Getting all mappings for TM2 code: {}", tm2Code);

                // Call searchByCode to get all three traditional medicine mappings
                List<NamasteCode> allMappings = searchByCode(matchedCode.getCode());

                if (!allMappings.isEmpty()) {
                    // Create a disease mapping group
                    DiseaseMapping diseaseMapping = new DiseaseMapping();
                    diseaseMapping.setTm2Code(tm2Code);
                    diseaseMapping.setTm2Title(matchedCode.getTm2Title());
                    diseaseMapping.setTm2Definition(matchedCode.getTm2Definition());
                    diseaseMapping.setSimilarityScore(matchedCode.getConfidenceScore()); // Symptom similarity score
                    diseaseMapping.setMappings(allMappings);

                    groupedResults.add(diseaseMapping);
                    if(groupedResults.size()>20)
                        break;
                    log.info("Added disease group for TM2 code: {} with {} traditional medicine mappings", tm2Code, allMappings.size());
                }
            }
        }

        log.info("FOUND {} DISEASE GROUPS (not individual mappings)", groupedResults.size());
        int totalMappings = groupedResults.stream().mapToInt(DiseaseMapping::getMappingCount).sum();
        log.info("Total individual mappings across all disease groups: {}", totalMappings);
        return groupedResults;
    }

    /**
     * Class to group disease mappings by TM2 code
     * Contains the TM2 disease information and all its traditional medicine mappings
     */
    public static class DiseaseMapping {
        private String tm2Code;
        private String tm2Title;
        private String tm2Definition;
        private Double similarityScore; // How well this disease matched the symptoms
        private List<NamasteCode> mappings; // All traditional medicine mappings for this disease

        public DiseaseMapping() {
            this.mappings = new ArrayList<>();
        }

        // Getters and Setters
        public String getTm2Code() {
            return tm2Code;
        }

        public void setTm2Code(String tm2Code) {
            this.tm2Code = tm2Code;
        }

        public String getTm2Title() {
            return tm2Title;
        }

        public void setTm2Title(String tm2Title) {
            this.tm2Title = tm2Title;
        }

        public String getTm2Definition() {
            return tm2Definition;
        }

        public void setTm2Definition(String tm2Definition) {
            this.tm2Definition = tm2Definition;
        }

        public Double getSimilarityScore() {
            return similarityScore;
        }

        public void setSimilarityScore(Double similarityScore) {
            this.similarityScore = similarityScore;
        }

        public List<NamasteCode> getMappings() {
            return mappings;
        }

        public void setMappings(List<NamasteCode> mappings) {
            this.mappings = mappings != null ? mappings : new ArrayList<>();
        }

        // Helper methods
        public int getMappingCount() {
            return mappings != null ? mappings.size() : 0;
        }
    }
}