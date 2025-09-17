package com.namaste.Namaste.to.TM2.Repository;

import com.namaste.Namaste.to.TM2.Model.NamasteCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NamasteCodeRepository extends MongoRepository<NamasteCode, String> {

    // Find by NAMASTE code
    Optional<NamasteCode> findByNamasteCode(String namasteCode);

    // Find by NAMASTE name (exact match)
    Optional<NamasteCode> findByNamasteName(String namasteName);

    // Auto-complete search (case-insensitive, contains)
    @Query("{'namasteName': {$regex: ?0, $options: 'i'}, 'isActive': true}")
    List<NamasteCode> findByNamasteNameContainingIgnoreCase(@Param("query") String query);

    // Find by category
    List<NamasteCode> findByNamasteCategoryAndIsActiveTrue(String category);

    // Find by ICD-11 TM2 code (reverse lookup)
    Optional<NamasteCode> findByIcd11Tm2Code(String icd11Tm2Code);

    // Find by ICD-11 Biomedicine code (reverse lookup) 
    Optional<NamasteCode> findByIcd11BiomedicineCode(String icd11BiomedicineCode);

    // Count by category
    long countByNamasteCategoryAndIsActiveTrue(String category);

    // Count total active records
    long countByIsActiveTrue();

    // Count records with dual coding
    @Query(value = "{'icd11Tm2Code': {$ne: null}, 'icd11BiomedicineCode': {$ne: null}, 'isActive': true}", count = true)
    long countDualCodedRecords();

    // Get all active codes ordered by name
    List<NamasteCode> findByIsActiveTrueOrderByNamasteNameAsc();

    // Get codes with ICD-11 TM2 mapping
    List<NamasteCode> findByIcd11Tm2CodeIsNotNullAndIsActiveTrue();

    // Get codes with ICD-11 Biomedicine mapping
    List<NamasteCode> findByIcd11BiomedicineCodeIsNotNullAndIsActiveTrue();

    // Get codes with both TM2 and Biomedicine mapping (dual coding)
    List<NamasteCode> findByIcd11Tm2CodeIsNotNullAndIcd11BiomedicineCodeIsNotNullAndIsActiveTrue();

    // Find unmapped records (missing ICD-11 mappings)
    @Query("{'$or': [{'icd11Tm2Code': null}, {'icd11BiomedicineCode': null}], 'isActive': true}")
    List<NamasteCode> findUnmappedRecords();

    // Advanced search across multiple fields
    @Query("{'$or': [" +
            "{'namasteName': {$regex: ?0, $options: 'i'}}, " +
            "{'namasteDescription': {$regex: ?0, $options: 'i'}}, " +
            "{'namasteCode': {$regex: ?0, $options: 'i'}}" +
            "], 'isActive': true}")
    List<NamasteCode> findByAdvancedSearch(@Param("query") String query);
}