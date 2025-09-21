package com.namaste.Namaste.to.TM2.Repository;

import com.namaste.Namaste.to.TM2.Model.NamasteCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom repository implementation for advanced symptom search operations
 */
@Repository
public class NamasteCodeRepositoryImpl implements NamasteCodeRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Search by multiple symptoms with AND logic using optimized MongoDB query
     * Documents must match ALL provided symptoms in any of the 4 text fields:
     * - code_description, tm2_definition, tm2_title, code_title
     * <p>
     * Query structure for symptoms ["fever", "headache", "nausea"]:
     * {
     * "$and": [
     * { "$or": [
     *     {"code_description": {$regex: "fever", $options: "i"}}, 
     *     {"tm2_definition": {$regex: "fever", $options: "i"}},
     *     {"tm2_title": {$regex: "fever", $options: "i"}},
     *     {"code_title": {$regex: "fever", $options: "i"}}
     * ]},
     * { "$or": [
     *     {"code_description": {$regex: "headache", $options: "i"}}, 
     *     {"tm2_definition": {$regex: "headache", $options: "i"}},
     *     {"tm2_title": {$regex: "headache", $options: "i"}},
     *     {"code_title": {$regex: "headache", $options: "i"}}
     * ]},
     * { "$or": [
     *     {"code_description": {$regex: "nausea", $options: "i"}}, 
     *     {"tm2_definition": {$regex: "nausea", $options: "i"}},
     *     {"tm2_title": {$regex: "nausea", $options: "i"}},
     *     {"code_title": {$regex: "nausea", $options: "i"}}
     * ]}
     * ]
     * }
     */
    @Override
    public List<NamasteCode> findByAllSymptoms(List<String> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) {
            return new ArrayList<>();
        }

        // Filter out very short symptoms and escape regex characters
        List<String> validSymptoms = symptoms.stream()
                .map(symptom -> escapeRegexSpecialChars(symptom.trim()))
                .collect(java.util.stream.Collectors.toList());

        if (validSymptoms.isEmpty()) {
            return new ArrayList<>();
        }

        // Build AND criteria for all symptoms
        List<Criteria> allSymptomsCriteria = new ArrayList<>();

        for (String symptom : validSymptoms) {
            // Each symptom must match in at least one of the 4 text fields
            Criteria codeDescCriteria = Criteria.where("code_description").regex(symptom, "i");
            Criteria tm2DefCriteria = Criteria.where("tm2_definition").regex(symptom, "i");
            Criteria tm2TitleCriteria = Criteria.where("tm2_title").regex(symptom, "i");
            Criteria codeTitleCriteria = Criteria.where("code_title").regex(symptom, "i");

            // This symptom criteria (must match in at least one of the 4 fields)
            Criteria thisSymptomCriteria = new Criteria().orOperator(
                    codeDescCriteria,
                    tm2DefCriteria,
                    tm2TitleCriteria,
                    codeTitleCriteria
            );
            allSymptomsCriteria.add(thisSymptomCriteria);
        }

        // Create the final query with AND logic
        Query query = new Query();
        if (allSymptomsCriteria.size() == 1) {
            // Single symptom case
            query.addCriteria(allSymptomsCriteria.get(0));
        } else {
            // Multiple symptoms - ALL must match (AND logic)
            Criteria finalCriteria = new Criteria().andOperator(allSymptomsCriteria.toArray(new Criteria[0]));
            query.addCriteria(finalCriteria);
        }

        // Execute the query
        return mongoTemplate.find(query, NamasteCode.class);
    }

    /**
     * Helper method to escape special regex characters
     */
    private String escapeRegexSpecialChars(String input) {
        return input.replaceAll("([\\[\\]\\(\\)\\{\\}\\+\\*\\?\\^\\$\\|\\.])", "\\\\$1");
    }
}