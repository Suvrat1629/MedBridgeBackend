package com.namaste.Namaste.to.TM2.Controller;

import com.namaste.Namaste.to.TM2.Model.NamasteCode;
import com.namaste.Namaste.to.TM2.Response.AbhaResponse;
import com.namaste.Namaste.to.TM2.Service.NamasteTerminologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/terminology")
@CrossOrigin(origins = "*")
public class NamasteTerminologyController {

    private static final Logger log = LoggerFactory.getLogger(NamasteTerminologyController.class);
    private final NamasteTerminologyService terminologyService;

    public NamasteTerminologyController(NamasteTerminologyService terminologyService) {
        this.terminologyService = terminologyService;
    }

    /**
     * Auto-complete endpoint for EMR UI
     * This is the primary endpoint for clinical workflows
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<AbhaResponse<List<NamasteCode>>> autoComplete(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Auto-complete request for query: {} with limit: {}", query, limit);

        try {
            List<NamasteCode> results = terminologyService.searchForAutoComplete(query, limit);
            return ResponseEntity.ok(AbhaResponse.success(results));
        } catch (Exception e) {
            log.error("Error in auto-complete search", e);
            return ResponseEntity.ok(AbhaResponse.error("Auto-complete search failed", "SEARCH_ERROR"));
        }
    }

    /**
     * Get complete details by NAMASTE name
     * Returns all mappings per problem statement requirements:
     * - NAMASTE codes (Indian traditional medicine)
     * - WHO International Terminologies for Ayurveda (global standardization)
     * - ICD-11 TM2 (traditional medicine classification)
     * - ICD-11 Biomedicine (insurance claims & dual coding requirement)
     */
    @GetMapping("/name/{namasteName}")
    public ResponseEntity<AbhaResponse<NamasteCode>> getByName(@PathVariable String namasteName) {
        log.info("Fetching details for NAMASTE name: {}", namasteName);

        try {
            Optional<NamasteCode> result = terminologyService.getByNamasteName(namasteName);

            if (result.isPresent()) {
                return ResponseEntity.ok(AbhaResponse.success(result.get()));
            } else {
                return ResponseEntity.ok(AbhaResponse.error("NAMASTE name not found", "NOT_FOUND"));
            }
        } catch (Exception e) {
            log.error("Error fetching by name", e);
            return ResponseEntity.ok(AbhaResponse.error("Failed to fetch terminology", "FETCH_ERROR"));
        }
    }

    /**
     * Get complete details by NAMASTE code
     */
    @GetMapping("/code/{namasteCode}")
    public ResponseEntity<AbhaResponse<NamasteCode>> getByCode(@PathVariable String namasteCode) {
        log.info("Fetching details for NAMASTE code: {}", namasteCode);

        try {
            Optional<NamasteCode> result = terminologyService.getByNamasteCode(namasteCode);

            if (result.isPresent()) {
                return ResponseEntity.ok(AbhaResponse.success(result.get()));
            } else {
                return ResponseEntity.ok(AbhaResponse.error("NAMASTE code not found", "NOT_FOUND"));
            }
        } catch (Exception e) {
            log.error("Error fetching by code", e);
            return ResponseEntity.ok(AbhaResponse.error("Failed to fetch terminology", "FETCH_ERROR"));
        }
    }

    /**
     * Comprehensive search across all fields
     */
    @GetMapping("/search")
    public ResponseEntity<AbhaResponse<List<NamasteCode>>> comprehensiveSearch(
            @RequestParam String query) {

        log.info("Comprehensive search for query: {}", query);

        try {
            List<NamasteCode> results = terminologyService.comprehensiveSearch(query);
            return ResponseEntity.ok(AbhaResponse.success(results));
        } catch (Exception e) {
            log.error("Error in comprehensive search", e);
            return ResponseEntity.ok(AbhaResponse.error("Comprehensive search failed", "SEARCH_ERROR"));
        }
    }

    /**
     * Get codes by traditional medicine category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<AbhaResponse<List<NamasteCode>>> getByCategory(@PathVariable String category) {
        log.info("Fetching codes for category: {}", category);

        try {
            List<NamasteCode> results = terminologyService.getByCategory(category);
            return ResponseEntity.ok(AbhaResponse.success(results));
        } catch (Exception e) {
            log.error("Error fetching by category", e);
            return ResponseEntity.ok(AbhaResponse.error("Failed to fetch by category", "FETCH_ERROR"));
        }
    }

    /**
     * Get records with dual coding (TM2 + Biomedicine)
     */
    @GetMapping("/dual-coding")
    public ResponseEntity<AbhaResponse<List<NamasteCode>>> getDualCoding() {
        log.info("Fetching records with dual coding");

        try {
            List<NamasteCode> results = terminologyService.getRecordsWithDualCoding();
            return ResponseEntity.ok(AbhaResponse.success(results));
        } catch (Exception e) {
            log.error("Error fetching dual coding records", e);
            return ResponseEntity.ok(AbhaResponse.error("Failed to fetch dual coding records", "FETCH_ERROR"));
        }
    }

    /**
     * Translation operation: NAMASTE → ICD-11 TM2
     */
    @GetMapping("/translate/tm2/{namasteCode}")
    public ResponseEntity<AbhaResponse<String>> translateToTm2(@PathVariable String namasteCode) {
        log.info("Translating NAMASTE code {} to ICD-11 TM2", namasteCode);

        try {
            Optional<String> tm2Code = terminologyService.translateToIcd11Tm2(namasteCode);

            if (tm2Code.isPresent()) {
                return ResponseEntity.ok(AbhaResponse.success(tm2Code.get()));
            } else {
                return ResponseEntity.ok(AbhaResponse.error("No TM2 mapping found", "NO_MAPPING"));
            }
        } catch (Exception e) {
            log.error("Error translating to TM2", e);
            return ResponseEntity.ok(AbhaResponse.error("Translation failed", "TRANSLATION_ERROR"));
        }
    }

    /**
     * Translation operation: NAMASTE → ICD-11 Biomedicine
     */
    @GetMapping("/translate/biomedicine/{namasteCode}")
    public ResponseEntity<AbhaResponse<String>> translateToBiomedicine(@PathVariable String namasteCode) {
        log.info("Translating NAMASTE code {} to ICD-11 Biomedicine", namasteCode);

        try {
            Optional<String> biomedicineCode = terminologyService.translateToIcd11Biomedicine(namasteCode);

            if (biomedicineCode.isPresent()) {
                return ResponseEntity.ok(AbhaResponse.success(biomedicineCode.get()));
            } else {
                return ResponseEntity.ok(AbhaResponse.error("No Biomedicine mapping found", "NO_MAPPING"));
            }
        } catch (Exception e) {
            log.error("Error translating to Biomedicine", e);
            return ResponseEntity.ok(AbhaResponse.error("Translation failed", "TRANSLATION_ERROR"));
        }
    }

    /**
     * Reverse translation: ICD-11 TM2 → NAMASTE
     */
    @GetMapping("/reverse/tm2/{icd11Tm2Code}")
    public ResponseEntity<AbhaResponse<NamasteCode>> reverseTranslateFromTm2(@PathVariable String icd11Tm2Code) {
        log.info("Reverse translating ICD-11 TM2 code {} to NAMASTE", icd11Tm2Code);

        try {
            Optional<NamasteCode> namasteCode = terminologyService.findByIcd11Tm2Code(icd11Tm2Code);

            if (namasteCode.isPresent()) {
                return ResponseEntity.ok(AbhaResponse.success(namasteCode.get()));
            } else {
                return ResponseEntity.ok(AbhaResponse.error("No NAMASTE mapping found", "NO_MAPPING"));
            }
        } catch (Exception e) {
            log.error("Error reverse translating from TM2", e);
            return ResponseEntity.ok(AbhaResponse.error("Reverse translation failed", "TRANSLATION_ERROR"));
        }
    }

    /**
     * Reverse translation: ICD-11 Biomedicine → NAMASTE
     */
    @GetMapping("/reverse/biomedicine/{icd11BiomedicineCode}")
    public ResponseEntity<AbhaResponse<NamasteCode>> reverseTranslateFromBiomedicine(@PathVariable String icd11BiomedicineCode) {
        log.info("Reverse translating ICD-11 Biomedicine code {} to NAMASTE", icd11BiomedicineCode);

        try {
            Optional<NamasteCode> namasteCode = terminologyService.findByIcd11BiomedicineCode(icd11BiomedicineCode);

            if (namasteCode.isPresent()) {
                return ResponseEntity.ok(AbhaResponse.success(namasteCode.get()));
            } else {
                return ResponseEntity.ok(AbhaResponse.error("No NAMASTE mapping found", "NO_MAPPING"));
            }
        } catch (Exception e) {
            log.error("Error reverse translating from Biomedicine", e);
            return ResponseEntity.ok(AbhaResponse.error("Reverse translation failed", "TRANSLATION_ERROR"));
        }
    }

    /**
     * Get terminology statistics for monitoring
     */
    @GetMapping("/stats")
    public ResponseEntity<AbhaResponse<NamasteTerminologyService.TerminologyStats>> getStats() {
        log.info("Fetching terminology statistics");

        try {
            NamasteTerminologyService.TerminologyStats stats = terminologyService.getTerminologyStats();
            return ResponseEntity.ok(AbhaResponse.success(stats));
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.ok(AbhaResponse.error("Failed to fetch statistics", "STATS_ERROR"));
        }
    }

    /**
     * Save or update NAMASTE code record (Admin operation)
     */
    @PostMapping("/admin/save")
    public ResponseEntity<AbhaResponse<NamasteCode>> saveOrUpdate(
            @RequestBody NamasteCode namasteCode,
            Authentication authentication) {

        String userId = authentication != null ? authentication.getName() : "system";
        log.info("Saving/updating NAMASTE code by user: {}", userId);

        try {
            NamasteCode savedCode = terminologyService.saveOrUpdate(namasteCode, userId);
            return ResponseEntity.ok(AbhaResponse.success(savedCode));
        } catch (Exception e) {
            log.error("Error saving/updating NAMASTE code", e);
            return ResponseEntity.ok(AbhaResponse.error("Failed to save terminology", "SAVE_ERROR"));
        }
    }

    /**
     * Get recently updated records for monitoring
     */
    @GetMapping("/admin/recent")
    public ResponseEntity<AbhaResponse<List<NamasteCode>>> getRecentlyUpdated(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Fetching {} recently updated records", limit);

        try {
            List<NamasteCode> results = terminologyService.getRecentlyUpdated(limit);
            return ResponseEntity.ok(AbhaResponse.success(results));
        } catch (Exception e) {
            log.error("Error fetching recently updated records", e);
            return ResponseEntity.ok(AbhaResponse.error("Failed to fetch recent records", "FETCH_ERROR"));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "NAMASTE Terminology Service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}