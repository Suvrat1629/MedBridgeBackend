package com.namaste.Namaste.to.TM2.Controller;

import com.namaste.Namaste.to.TM2.Model.NamasteCode;
import com.namaste.Namaste.to.TM2.Response.AbhaResponse;
import com.namaste.Namaste.to.TM2.Service.NamasteTerminologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/terminology")
@CrossOrigin(origins = "*")
public class NamasteTerminologyController {
//
//    private static final Logger log = LoggerFactory.getLogger(NamasteTerminologyController.class);
//    private final NamasteTerminologyService terminologyService;
//
//    public NamasteTerminologyController(NamasteTerminologyService terminologyService) {
//        this.terminologyService = terminologyService;
//    }
//
//    /**
//     * MAIN FEATURE 1: Search by code (tm2_code or code field)
//     * This endpoint searches in both tm2_code and code fields and returns the matching document
//     */
//    @GetMapping("/search/code/{codeValue}")
//    public ResponseEntity<AbhaResponse<NamasteCode>> searchByCode(@PathVariable String codeValue) {
//        log.info("Code search request for: {}", codeValue);
//
//        try {
//            Optional<NamasteCode> result = terminologyService.searchByCode(codeValue);
//
//            if (result.isPresent()) {
//                return ResponseEntity.ok(AbhaResponse.success(result.get()));
//            } else {
//                return ResponseEntity.ok(AbhaResponse.error("Code not found: " + codeValue, "NOT_FOUND"));
//            }
//        } catch (Exception e) {
//            log.error("Error in code search", e);
//            return ResponseEntity.ok(AbhaResponse.error("Code search failed", "SEARCH_ERROR"));
//        }
//    }
//
//    /**
//     * MAIN FEATURE 2: Search by symptoms/description (code_description or tm2_definition field)
//     * This endpoint searches in both description fields with fuzzy matching
//     */
//    @GetMapping("/search/symptoms")
//    public ResponseEntity<AbhaResponse<List<NamasteCode>>> searchBySymptoms(
//            @RequestParam String query) {
//
//        log.info("Symptom search request for: {}", query);
//
//        try {
//            List<NamasteCode> results = terminologyService.searchBySymptoms(query);
//            return ResponseEntity.ok(AbhaResponse.success(results));
//        } catch (Exception e) {
//            log.error("Error in symptom search", e);
//            return ResponseEntity.ok(AbhaResponse.error("Symptom search failed", "SEARCH_ERROR"));
//        }
//    }
//
//    /**
//     * Auto-complete endpoint for EMR UI
//     * This is the primary endpoint for clinical workflows
//     */
//    @GetMapping("/autocomplete")
//    public ResponseEntity<AbhaResponse<List<NamasteCode>>> autoComplete(
//            @RequestParam String query,
//            @RequestParam(defaultValue = "10") int limit) {
//
//        log.info("Auto-complete request for query: {} with limit: {}", query, limit);
//
//        try {
//            List<NamasteCode> results = terminologyService.searchForAutoComplete(query, limit);
//            return ResponseEntity.ok(AbhaResponse.success(results));
//        } catch (Exception e) {
//            log.error("Error in auto-complete search", e);
//            return ResponseEntity.ok(AbhaResponse.error("Auto-complete search failed", "SEARCH_ERROR"));
//        }
//    }
//
//    /**
//     * Health check endpoint
//     */
//    @GetMapping("/health")
//    public ResponseEntity<Map<String, String>> health() {
//        return ResponseEntity.ok(Map.of(
//                "status", "UP",
//                "service", "NAMASTE Terminology Service",
//                "timestamp", java.time.LocalDateTime.now().toString()
//        ));
//    }
}