package com.namaste.Namaste.to.TM2.Repository;

import com.namaste.Namaste.to.TM2.Model.NamasteCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface NamasteCodeRepository extends MongoRepository<NamasteCode, String> {

    /**
     * Search by code - checks both tm2_code and code fields (EXACT MATCH ONLY)
     * This is one of the two main search features requested
     */
    @Query("{'$or': [" +
            "{'tm2_code': ?0}, " +
            "{'code': ?0}" +
            "]}")
    Optional<List<NamasteCode>> findByAnyCode(@Param("codeValue") String codeValue);

    /**
     * Search by symptoms/description - checks both code_description and tm2_definition fields
     * Uses case-insensitive regex matching for fuzzy search
     * This is the second main search feature requested
     */
    @Query("{'$or': [" +
            "{'code_description': {$regex: ?0, $options: 'i'}}, " +
            "{'tm2_definition': {$regex: ?0, $options: 'i'}}" +
            "]}")
    List<NamasteCode> findBySymptoms(@Param("symptomQuery") String symptomQuery);

    // Find by traditional medicine code (formerly NAMASTE code)
    Optional<NamasteCode> findByCode(String code);

    // Find by traditional medicine name (code_title)
    Optional<NamasteCode> findByCodeTitle(String codeTitle);

    // Auto-complete search (case-insensitive, contains)
    @Query("{'code_title': {$regex: ?0, $options: 'i'}}")
    List<NamasteCode> findByCodeTitleContainingIgnoreCase(@Param("query") String query);

    // Find by type (category - ayurveda, siddha, unani, etc.)
    List<NamasteCode> findByType(String type);

    // Find by TM2 code (reverse lookup)
    Optional<NamasteCode> findByTm2Code(String tm2Code);

    // Count by type (category)
    long countByType(String type);

    // Count total records
    long count();

    // Get all codes ordered by code_title
    List<NamasteCode> findAllByOrderByCodeTitleAsc();

    // Get codes with TM2 mapping
    List<NamasteCode> findByTm2CodeIsNotNull();

    // Advanced search across multiple fields
    @Query("{'$or': [" +
            "{'code_title': {$regex: ?0, $options: 'i'}}, " +
            "{'code_description': {$regex: ?0, $options: 'i'}}, " +
            "{'code': {$regex: ?0, $options: 'i'}}, " +
            "{'tm2_title': {$regex: ?0, $options: 'i'}}" +
            "]}")
    List<NamasteCode> findByAdvancedSearch(@Param("query") String query);

    // Find high confidence mappings (>= 0.8)
    @Query("{'confidence_score': {$gte: 0.8}}")
    List<NamasteCode> findHighConfidenceMappings();

    // Find medium confidence mappings (0.6 - 0.8)
    @Query("{'confidence_score': {$gte: 0.6, $lt: 0.8}}")
    List<NamasteCode> findMediumConfidenceMappings();

    // Find low confidence mappings (< 0.6)
    @Query("{'confidence_score': {$lt: 0.6}}")
    List<NamasteCode> findLowConfidenceMappings();
}