package com.namaste.Namaste.to.TM2.Service;

import com.namaste.Namaste.to.TM2.Model.NamasteCode;
import com.namaste.Namaste.to.TM2.Repository.NamasteCodeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NamasteTerminologyService {

    private static final Logger log = LoggerFactory.getLogger(NamasteTerminologyService.class);
    private final NamasteCodeRepository namasteCodeRepository;

    public NamasteTerminologyService(NamasteCodeRepository namasteCodeRepository) {
        this.namasteCodeRepository = namasteCodeRepository;
    }

    /**
     * Auto-complete search for EMR UI - Primary method for clinical workflows
     * Returns matching traditional medicine codes with their TM2 mappings
     */
    public List<NamasteCode> searchForAutoComplete(String searchTerm, int maxResults) {
        log.info("Auto-complete search for term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().length() < 2) {
            return List.of(); // Return empty list for very short search terms
        }

        // Use the updated repository method
        List<NamasteCode> results = namasteCodeRepository.findByCodeTitleContainingIgnoreCase(searchTerm.trim());

        // Limit results for performance
        return results.stream().limit(maxResults).collect(Collectors.toList());
    }

    /**
     * Get complete details by traditional medicine name - Main method for retrieving all mappings
     */
    public Optional<NamasteCode> getByNamasteName(String namasteName) {
        log.info("Fetching details for traditional medicine name: {}", namasteName);
        return namasteCodeRepository.findByCodeTitle(namasteName);
    }

    /**
     * Get complete details by traditional medicine code
     */
    public Optional<NamasteCode> getByNamasteCode(String namasteCode) {
        log.info("Fetching details for traditional medicine code: {}", namasteCode);
        return namasteCodeRepository.findByCode(namasteCode);
    }

    /**
     * Comprehensive search across all fields
     */
    public List<NamasteCode> comprehensiveSearch(String searchTerm) {
        log.info("Comprehensive search for term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().length() < 2) {
            return List.of();
        }

        return namasteCodeRepository.findByAdvancedSearch(searchTerm.trim());
    }

    /**
     * Get codes by traditional medicine category (type)
     */
    public List<NamasteCode> getByCategory(String category) {
        log.info("Fetching codes for category: {}", category);
        return namasteCodeRepository.findByType(category);
    }

    /**
     * Get records with dual coding - Note: Current document structure doesn't support dual coding
     */
    public List<NamasteCode> getRecordsWithDualCoding() {
        log.info("Fetching records with dual coding - returning empty list as not supported in current structure");
        return List.of();
    }

    /**
     * Translate traditional medicine code to ICD-11 TM2
     */
    public Optional<String> translateToIcd11Tm2(String traditionalMedicineCode) {
        log.info("Translating traditional medicine code {} to ICD-11 TM2", traditionalMedicineCode);

        return namasteCodeRepository.findByCode(traditionalMedicineCode)
                .map(NamasteCode::getTm2Code);
    }

    /**
     * Translate traditional medicine code to ICD-11 Biomedicine - Not supported in current structure
     */
    public Optional<String> translateToIcd11Biomedicine(String traditionalMedicineCode) {
        log.info("Translating traditional medicine code {} to ICD-11 Biomedicine - not supported", traditionalMedicineCode);
        return Optional.empty();
    }

    /**
     * Reverse translate: Find traditional medicine code from ICD-11 TM2
     */
    public Optional<NamasteCode> findByIcd11Tm2Code(String icd11Tm2Code) {
        log.info("Finding traditional medicine code for ICD-11 TM2: {}", icd11Tm2Code);
        return namasteCodeRepository.findByTm2Code(icd11Tm2Code);
    }

    /**
     * Reverse translate: Find traditional medicine code from ICD-11 Biomedicine - Not supported
     */
    public Optional<NamasteCode> findByIcd11BiomedicineCode(String icd11BiomedicineCode) {
        log.info("Finding traditional medicine code for ICD-11 Biomedicine - not supported: {}", icd11BiomedicineCode);
        return Optional.empty();
    }

    /**
     * Get all active traditional medicine codes
     */
    public List<NamasteCode> getAllActiveCodes() {
        log.info("Fetching all traditional medicine codes");
        return namasteCodeRepository.findAllByOrderByCodeTitleAsc();
    }

    /**
     * Get codes that have ICD-11 TM2 mapping
     */
    public List<NamasteCode> getCodesWithTm2Mapping() {
        log.info("Fetching codes with ICD-11 TM2 mapping");
        return namasteCodeRepository.findByTm2CodeIsNotNull();
    }

    /**
     * Get codes that have ICD-11 Biomedicine mapping - Not supported
     */
    public List<NamasteCode> getCodesWithBiomedicineMapping() {
        log.info("Fetching codes with ICD-11 Biomedicine mapping - not supported");
        return List.of();
    }

    /**
     * Get high confidence mappings (>= 0.8)
     */
    public List<NamasteCode> getHighConfidenceMappings() {
        log.info("Fetching high confidence mappings");
        return namasteCodeRepository.findHighConfidenceMappings();
    }

    /**
     * Get medium confidence mappings (0.6 - 0.8)
     */
    public List<NamasteCode> getMediumConfidenceMappings() {
        log.info("Fetching medium confidence mappings");
        return namasteCodeRepository.findMediumConfidenceMappings();
    }

    /**
     * Get low confidence mappings (< 0.6)
     */
    public List<NamasteCode> getLowConfidenceMappings() {
        log.info("Fetching low confidence mappings");
        return namasteCodeRepository.findLowConfidenceMappings();
    }

    /**
     * Get terminology statistics for dashboard/monitoring
     */
    public TerminologyStats getTerminologyStats() {
        log.info("Generating terminology statistics");

        TerminologyStats stats = new TerminologyStats();
        stats.setTotalCodes(namasteCodeRepository.count());
        stats.setAyurvedaCodes(namasteCodeRepository.countByType("ayurveda"));
        stats.setSiddhaCodes(namasteCodeRepository.countByType("siddha"));
        stats.setUnaniCodes(namasteCodeRepository.countByType("unani"));
        stats.setDualCodedRecords(0); // Not supported in current structure
        stats.setUnmappedRecords(0); // All records have TM2 mappings
        stats.setHighConfidenceMappings(namasteCodeRepository.findHighConfidenceMappings().size());
        stats.setMediumConfidenceMappings(namasteCodeRepository.findMediumConfidenceMappings().size());
        stats.setLowConfidenceMappings(namasteCodeRepository.findLowConfidenceMappings().size());

        return stats;
    }

    /**
     * Save or update a traditional medicine code record
     */
    public NamasteCode saveOrUpdate(NamasteCode namasteCode, String userId) {
        log.info("Saving/updating traditional medicine code: {}", namasteCode.getCode());
        return namasteCodeRepository.save(namasteCode);
    }

    /**
     * Get recently updated records for auditing
     */
    public List<NamasteCode> getRecentlyUpdated(int limit) {
        log.info("Fetching {} recently updated records", limit);
        List<NamasteCode> allCodes = namasteCodeRepository.findAllByOrderByCodeTitleAsc();
        return allCodes.stream().limit(limit).collect(Collectors.toList());
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
                    if(tm2docu.isPresent()&&code.getType()==tm2docu.get().getType())
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
     * Search by TM2 code only
     * Searches only in tm2_code field (EXACT MATCH ONLY)
     */
    public List<NamasteCode> searchByTm2CodeOnly(String codeValue) {
        log.info("=== SEARCH BY TM2 CODE ONLY DEBUG START ===");
        log.info("Input codeValue: '{}'", codeValue);

        if (codeValue == null || codeValue.trim().isEmpty()) {
            log.warn("Code value is null or empty, returning empty result");
            return List.of();
        }

        String trimmedCode = codeValue.trim();
        log.info("Calling repository.findByTm2CodeOnly with parameter: '{}'", trimmedCode);

        try {
            Optional<List<NamasteCode>> result = namasteCodeRepository.findByTm2CodeOnly(trimmedCode);

            if (result.isEmpty() || result.get().isEmpty()) {
                log.warn("No results found for TM2 code: '{}'", trimmedCode);
                return List.of();
            }

            List<NamasteCode> results = result.get();
            log.info("Results found: {}", results.size());

            // Filter results to only include codes with confidence score > 0.6 and limit to 6
            List<NamasteCode> filteredResults = results.stream()
                    .filter(code -> code.getConfidenceScore() != null && code.getConfidenceScore() > 0.6)
                    .limit(6)
                    .collect(Collectors.toList());

            log.info("Results after confidence filter (>0.6): {}", filteredResults.size());
            log.info("=== SEARCH BY TM2 CODE ONLY DEBUG END ===");
            return filteredResults;

        } catch (Exception e) {
            log.error("Exception occurred while searching for TM2 code: '{}'", trimmedCode, e);
            throw e;
        }
    }

    /**
     * Search by code only
     * Searches only in code field (EXACT MATCH ONLY)
     */
    public List<NamasteCode> searchByCodeOnly(String codeValue) {
        log.info("=== SEARCH BY CODE ONLY DEBUG START ===");
        log.info("Input codeValue: '{}'", codeValue);

        if (codeValue == null || codeValue.trim().isEmpty()) {
            log.warn("Code value is null or empty, returning empty result");
            return List.of();
        }

        String trimmedCode = codeValue.trim();
        log.info("Calling repository.findByCodeOnly with parameter: '{}'", trimmedCode);

        try {
            Optional<List<NamasteCode>> result = namasteCodeRepository.findByCodeOnly(trimmedCode);

            if (result.isEmpty() || result.get().isEmpty()) {
                log.warn("No results found for code: '{}'", trimmedCode);
                return List.of();
            }

            List<NamasteCode> results = result.get();
            log.info("Results found: {}", results.size());

            // Filter results to only include codes with confidence score > 0.6 and limit to 6
            List<NamasteCode> filteredResults = results.stream()
                    .filter(code -> code.getConfidenceScore() != null && code.getConfidenceScore() > 0.6)
                    .limit(6)
                    .collect(Collectors.toList());

            log.info("Results after confidence filter (>0.6): {}", filteredResults.size());
            log.info("=== SEARCH BY CODE ONLY DEBUG END ===");
            return filteredResults;

        } catch (Exception e) {
            log.error("Exception occurred while searching for code: '{}'", trimmedCode, e);
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
     * Keep the original method for backward compatibility
     */
    public List<NamasteCode> searchBySymptoms(List<String> symptoms) {
        // Convert grouped results back to flat list for backward compatibility
        return searchBySymptomsGrouped(symptoms).stream()
                .flatMap(diseaseMapping -> diseaseMapping.getMappings().stream())
                .collect(Collectors.toList());
    }

    /**
     * Calculate similarity score between multiple symptoms and document text fields
     * Returns a score between 0.0 and 1.0 where 1.0 is perfect match
     */
    private double calculateMultiSymptomSimilarityScore(List<String> symptoms, NamasteCode code) {
        double totalScore = 0.0;
        int scoredSymptoms = 0;

        // Calculate score for each symptom and take the average
        for (String symptom : symptoms) {
            String symptomLower = symptom.toLowerCase();
            double symptomScore = calculateSingleSymptomScore(symptomLower, code);

            if (symptomScore > 0.0) {
                totalScore += symptomScore;
                scoredSymptoms++;
            }
        }

        if (scoredSymptoms == 0) {
            return 0.0;
        }

        // Calculate average score
        double averageScore = totalScore / scoredSymptoms;

        // Bonus for matching multiple symptoms
        double multiSymptomBonus = 0.0;
        if (scoredSymptoms > 1) {
            // Give bonus based on how many symptoms matched
            multiSymptomBonus = (double) (scoredSymptoms - 1) * 0.1; // 10% bonus per additional symptom
            multiSymptomBonus = Math.min(multiSymptomBonus, 0.3); // Cap at 30% bonus
        }

        return Math.min(1.0, averageScore + multiSymptomBonus);
    }

    /**
     * Calculate similarity score for a single symptom against a document
     */
    private double calculateSingleSymptomScore(String symptom, NamasteCode code) {
        double maxScore = 0.0;

        // Check code_description field
        if (code.getCodeDescription() != null) {
            double descScore = calculateTextSimilarity(symptom, code.getCodeDescription().toLowerCase());
            maxScore = Math.max(maxScore, descScore);
        }

        // Check tm2_definition field
        if (code.getTm2Definition() != null) {
            double defScore = calculateTextSimilarity(symptom, code.getTm2Definition().toLowerCase());
            maxScore = Math.max(maxScore, defScore);
        }

        // Check code_title field for additional matching
        if (code.getCodeTitle() != null) {
            double titleScore = calculateTextSimilarity(symptom, code.getCodeTitle().toLowerCase());
            maxScore = Math.max(maxScore, titleScore * 0.8); // Slightly lower weight for title
        }

        // Check tm2_title field
        if (code.getTm2Title() != null) {
            double tm2TitleScore = calculateTextSimilarity(symptom, code.getTm2Title().toLowerCase());
            maxScore = Math.max(maxScore, tm2TitleScore * 0.8); // Slightly lower weight for title
        }

        return maxScore;
    }

    /**
     * Calculate similarity score between query and document text fields
     * Returns a score between 0.0 and 1.0 where 1.0 is perfect match
     */
    private double calculateSimilarityScore(String query, NamasteCode code) {
        double maxScore = 0.0;

        // Check code_description field
        if (code.getCodeDescription() != null) {
            double descScore = calculateTextSimilarity(query, code.getCodeDescription().toLowerCase());
            maxScore = Math.max(maxScore, descScore);
        }

        // Check tm2_definition field
        if (code.getTm2Definition() != null) {
            double defScore = calculateTextSimilarity(query, code.getTm2Definition().toLowerCase());
            maxScore = Math.max(maxScore, defScore);
        }

        // Check code_title field for additional matching
        if (code.getCodeTitle() != null) {
            double titleScore = calculateTextSimilarity(query, code.getCodeTitle().toLowerCase());
            maxScore = Math.max(maxScore, titleScore * 0.8); // Slightly lower weight for title
        }

        // Check tm2_title field
        if (code.getTm2Title() != null) {
            double tm2TitleScore = calculateTextSimilarity(query, code.getTm2Title().toLowerCase());
            maxScore = Math.max(maxScore, tm2TitleScore * 0.8); // Slightly lower weight for title
        }

        return maxScore;
    }

    /**
     * Simple text similarity calculation using word overlap and containment
     * This is a basic implementation - for production, consider using more sophisticated
     * algorithms like Jaccard similarity, Levenshtein distance, or TF-IDF
     */
    private double calculateTextSimilarity(String query, String text) {
        if (query == null || text == null || query.trim().isEmpty() || text.trim().isEmpty()) {
            return 0.0;
        }

        String queryTrimmed = query.trim();
        String textTrimmed = text.trim();

        // Exact match gets highest score
        if (textTrimmed.contains(queryTrimmed)) {
            return 1.0;
        }

        // Split into words and calculate overlap
        String[] queryWords = queryTrimmed.split("\\s+");
        String[] textWords = textTrimmed.split("\\s+");

        int matchingWords = 0;
        for (String queryWord : queryWords) {
            if (queryWord.length() > 2) { // Only consider words longer than 2 characters
                for (String textWord : textWords) {
                    if (textWord.contains(queryWord) || queryWord.contains(textWord)) {
                        matchingWords++;
                        break; // Count each query word only once
                    }
                }
            }
        }

        // Calculate similarity as ratio of matching words
        double wordSimilarity = queryWords.length > 0 ? (double) matchingWords / queryWords.length : 0.0;

        // Boost score if query is a substring of text (partial match)
        double substringBonus = 0.0;
        if (textTrimmed.contains(queryTrimmed)) {
            substringBonus = 0.3;
        }

        return Math.min(1.0, wordSimilarity + substringBonus);
    }

    /**
     * Helper method to escape special regex characters
     */
    private String escapeRegexSpecialChars(String input) {
        return input.replaceAll("([\\[\\]\\(\\)\\{\\}\\+\\*\\?\\^\\$\\|\\.])", "\\\\$1");
    }

    /**
     * Enhanced Statistics class for monitoring and dashboard
     */
    public static class TerminologyStats {
        private long totalCodes;
        private long ayurvedaCodes;
        private long siddhaCodes;
        private long unaniCodes;
        private long dualCodedRecords;
        private long unmappedRecords;
        private long highConfidenceMappings;
        private long mediumConfidenceMappings;
        private long lowConfidenceMappings;

        // Getters and Setters
        public long getTotalCodes() {
            return totalCodes;
        }

        public void setTotalCodes(long totalCodes) {
            this.totalCodes = totalCodes;
        }

        public long getAyurvedaCodes() {
            return ayurvedaCodes;
        }

        public void setAyurvedaCodes(long ayurvedaCodes) {
            this.ayurvedaCodes = ayurvedaCodes;
        }

        public long getSiddhaCodes() {
            return siddhaCodes;
        }

        public void setSiddhaCodes(long siddhaCodes) {
            this.siddhaCodes = siddhaCodes;
        }

        public long getUnaniCodes() {
            return unaniCodes;
        }

        public void setUnaniCodes(long unaniCodes) {
            this.unaniCodes = unaniCodes;
        }

        public long getDualCodedRecords() {
            return dualCodedRecords;
        }

        public void setDualCodedRecords(long dualCodedRecords) {
            this.dualCodedRecords = dualCodedRecords;
        }

        public long getUnmappedRecords() {
            return unmappedRecords;
        }

        public void setUnmappedRecords(long unmappedRecords) {
            this.unmappedRecords = unmappedRecords;
        }

        public long getHighConfidenceMappings() {
            return highConfidenceMappings;
        }

        public void setHighConfidenceMappings(long highConfidenceMappings) {
            this.highConfidenceMappings = highConfidenceMappings;
        }

        public long getMediumConfidenceMappings() {
            return mediumConfidenceMappings;
        }

        public void setMediumConfidenceMappings(long mediumConfidenceMappings) {
            this.mediumConfidenceMappings = mediumConfidenceMappings;
        }

        public long getLowConfidenceMappings() {
            return lowConfidenceMappings;
        }

        public void setLowConfidenceMappings(long lowConfidenceMappings) {
            this.lowConfidenceMappings = lowConfidenceMappings;
        }
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

        public boolean hasAyurvedaMapping() {
            return mappings != null && mappings.stream().anyMatch(m -> "ayurveda".equalsIgnoreCase(m.getType()));
        }

        public boolean hasSiddhaMapping() {
            return mappings != null && mappings.stream().anyMatch(m -> "siddha".equalsIgnoreCase(m.getType()));
        }

        public boolean hasUnaniMapping() {
            return mappings != null && mappings.stream().anyMatch(m -> "unani".equalsIgnoreCase(m.getType()));
        }
    }
}