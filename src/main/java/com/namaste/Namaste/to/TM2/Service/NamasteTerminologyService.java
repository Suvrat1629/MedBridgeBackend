package com.namaste.Namaste.to.TM2.Service;

import com.namaste.Namaste.to.TM2.Model.NamasteCode;
import com.namaste.Namaste.to.TM2.Repository.NamasteCodeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NamasteTerminologyService {

    private static final Logger log = LoggerFactory.getLogger(NamasteTerminologyService.class);
    private final NamasteCodeRepository namasteCodeRepository;

    public NamasteTerminologyService(NamasteCodeRepository namasteCodeRepository) {
        this.namasteCodeRepository = namasteCodeRepository;
    }

    /**
     * Auto-complete search for EMR UI - Primary method for clinical workflows
     * Returns matching NAMASTE codes with all their ICD-11 mappings
     */
    public List<NamasteCode> searchForAutoComplete(String searchTerm, int maxResults) {
        log.info("Auto-complete search for term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().length() < 2) {
            return List.of(); // Return empty list for very short search terms
        }

        // Use pagination to limit results for performance
        List<NamasteCode> results = namasteCodeRepository.findByNamasteNameContainingIgnoreCase(searchTerm.trim());

        // Limit results for performance
        return results.stream().limit(maxResults).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get complete details by NAMASTE name - Main method for retrieving all mappings
     */
    public Optional<NamasteCode> getByNamasteName(String namasteName) {
        log.info("Fetching details for NAMASTE name: {}", namasteName);
        return namasteCodeRepository.findByNamasteName(namasteName);
    }

    /**
     * Get complete details by NAMASTE code
     */
    public Optional<NamasteCode> getByNamasteCode(String namasteCode) {
        log.info("Fetching details for NAMASTE code: {}", namasteCode);
        return namasteCodeRepository.findByNamasteCode(namasteCode);
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
     * Get codes by traditional medicine category
     */
    public List<NamasteCode> getByCategory(String category) {
        log.info("Fetching codes for category: {}", category);
        return namasteCodeRepository.findByNamasteCategoryAndIsActiveTrue(category);
    }

    /**
     * Get records with dual coding (both TM2 and Biomedicine codes available)
     */
    public List<NamasteCode> getRecordsWithDualCoding() {
        log.info("Fetching records with dual coding");
        // Use a query to find records with both mappings
        return namasteCodeRepository.findByIcd11Tm2CodeIsNotNullAndIcd11BiomedicineCodeIsNotNullAndIsActiveTrue();
    }

    /**
     * Translate NAMASTE code to ICD-11 TM2
     */
    public Optional<String> translateToIcd11Tm2(String namasteCode) {
        log.info("Translating NAMASTE code {} to ICD-11 TM2", namasteCode);

        return namasteCodeRepository.findByNamasteCode(namasteCode)
                .map(NamasteCode::getIcd11Tm2Code);
    }

    /**
     * Translate NAMASTE code to ICD-11 Biomedicine
     */
    public Optional<String> translateToIcd11Biomedicine(String namasteCode) {
        log.info("Translating NAMASTE code {} to ICD-11 Biomedicine", namasteCode);

        return namasteCodeRepository.findByNamasteCode(namasteCode)
                .map(NamasteCode::getIcd11BiomedicineCode);
    }

    /**
     * Reverse translate: Find NAMASTE code from ICD-11 TM2
     */
    public Optional<NamasteCode> findByIcd11Tm2Code(String icd11Tm2Code) {
        log.info("Finding NAMASTE code for ICD-11 TM2: {}", icd11Tm2Code);
        return namasteCodeRepository.findByIcd11Tm2Code(icd11Tm2Code);
    }

    /**
     * Reverse translate: Find NAMASTE code from ICD-11 Biomedicine
     */
    public Optional<NamasteCode> findByIcd11BiomedicineCode(String icd11BiomedicineCode) {
        log.info("Finding NAMASTE code for ICD-11 Biomedicine: {}", icd11BiomedicineCode);
        return namasteCodeRepository.findByIcd11BiomedicineCode(icd11BiomedicineCode);
    }

    /**
     * Get all active NAMASTE codes
     */
    public List<NamasteCode> getAllActiveCodes() {
        log.info("Fetching all active NAMASTE codes");
        return namasteCodeRepository.findByIsActiveTrueOrderByNamasteNameAsc();
    }

    /**
     * Get codes that have ICD-11 TM2 mapping
     */
    public List<NamasteCode> getCodesWithTm2Mapping() {
        log.info("Fetching codes with ICD-11 TM2 mapping");
        return namasteCodeRepository.findByIcd11Tm2CodeIsNotNullAndIsActiveTrue();
    }

    /**
     * Get codes that have ICD-11 Biomedicine mapping
     */
    public List<NamasteCode> getCodesWithBiomedicineMapping() {
        log.info("Fetching codes with ICD-11 Biomedicine mapping");
        return namasteCodeRepository.findByIcd11BiomedicineCodeIsNotNullAndIsActiveTrue();
    }

    /**
     * Get terminology statistics for dashboard/monitoring
     */
    public TerminologyStats getTerminologyStats() {
        log.info("Generating terminology statistics");

        TerminologyStats stats = new TerminologyStats();
        stats.setTotalCodes(namasteCodeRepository.countByIsActiveTrue());
        stats.setAyurvedaCodes(namasteCodeRepository.countByNamasteCategoryAndIsActiveTrue("Ayurveda"));
        stats.setSiddhaCodes(namasteCodeRepository.countByNamasteCategoryAndIsActiveTrue("Siddha"));
        stats.setUnaniCodes(namasteCodeRepository.countByNamasteCategoryAndIsActiveTrue("Unani"));
        stats.setDualCodedRecords(namasteCodeRepository.findByIcd11Tm2CodeIsNotNullAndIcd11BiomedicineCodeIsNotNullAndIsActiveTrue().size());
        stats.setUnmappedRecords(namasteCodeRepository.findUnmappedRecords().size());

        return stats;
    }

    /**
     * Save or update a NAMASTE code record
     */
    @Transactional
    public NamasteCode saveOrUpdate(NamasteCode namasteCode, String userId) {
        log.info("Saving/updating NAMASTE code: {}", namasteCode.getNamasteCode());

        if (namasteCode.getId() == null) {
            // New record
            namasteCode.setCreatedBy(userId);
            namasteCode.setCreatedAt(LocalDateTime.now());
        } else {
            // Update existing record
            namasteCode.setUpdatedBy(userId);
            namasteCode.setUpdatedAt(LocalDateTime.now());
        }

        return namasteCodeRepository.save(namasteCode);
    }

    /**
     * Get recently updated records for auditing
     */
    public List<NamasteCode> getRecentlyUpdated(int limit) {
        log.info("Fetching {} recently updated records", limit);
        // Simple approach - get all active codes sorted by name (since we don't have updatedAt sorting)
        List<NamasteCode> allCodes = namasteCodeRepository.findByIsActiveTrueOrderByNamasteNameAsc();
        return allCodes.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Statistics class for monitoring and dashboard
     */
    public static class TerminologyStats {
        private long totalCodes;
        private long ayurvedaCodes;
        private long siddhaCodes;
        private long unaniCodes;
        private long dualCodedRecords;
        private long unmappedRecords;

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
    }
}