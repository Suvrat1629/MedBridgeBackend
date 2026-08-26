package com.namaste.Namaste.to.TM2.Repository;

import com.namaste.Namaste.to.TM2.Model.NamasteCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NamasteCodeRepository extends MongoRepository<NamasteCode, String>, NamasteCodeRepositoryCustom {

    /**
     * Search only in tm2_code field (EXACT MATCH ONLY)
     * Returns top 6 results sorted by confidence score (highest first)
     */
    @Query(value = "{'tm2_code': ?0}", sort = "{'confidence_score': -1}")
    Optional<List<NamasteCode>> findByTm2CodeOnly(@Param("codeValue") String codeValue);

    /**
     * Multi-symptom search with AND logic is available via findByAllSymptoms() method
     * from NamasteCodeRepositoryCustom interface
     */

    /**
     * Find TM2 code with maximum confidence level by searching in code field
     * Returns single result with highest confidence score
     */
    Optional<NamasteCode> findTopByCodeOrderByConfidenceScoreDesc(String code);

}
