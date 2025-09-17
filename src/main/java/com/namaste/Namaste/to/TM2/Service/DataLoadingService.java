package com.namaste.Namaste.to.TM2.Service;

import com.namaste.Namaste.to.TM2.Model.NamasteCode;
import com.namaste.Namaste.to.TM2.Repository.NamasteCodeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataLoadingService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoadingService.class);
    private final NamasteCodeRepository namasteCodeRepository;

    public DataLoadingService(NamasteCodeRepository namasteCodeRepository) {
        this.namasteCodeRepository = namasteCodeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Load sample data if database is empty
        if (namasteCodeRepository.count() == 0) {
            log.info("Loading sample NAMASTE data with dual coding mappings");
            loadSampleData();
        } else {
            log.info("NAMASTE database already contains {} records", namasteCodeRepository.count());
        }
    }

    private void loadSampleData() {
        List<NamasteCode> sampleCodes = List.of(
                createSampleCode("NAM001", "Vataj Jwara", "Fever due to Vata dosha imbalance", "Ayurveda",
                        "Fever syndromes", "WHO-AY-001", "Vata-predominant fever",
                        "XM4KH5", "Fever, unspecified", "http://id.who.int/icd/entity/455013390",
                        "XN1Z8", "Essential fever", "http://id.who.int/icd/entity/455013391",
                        "HIGH", "EXACT", "Direct mapping validated by Ayurvedic experts"),

                createSampleCode("NAM002", "Pittaj Shirashool", "Headache due to Pitta dosha imbalance", "Ayurveda",
                        "Head disorders", "WHO-AY-002", "Pitta-predominant headache",
                        "XM5RT2", "Headache disorders", "http://id.who.int/icd/entity/455013392",
                        "XN2M9", "Primary headache", "http://id.who.int/icd/entity/455013393",
                        "HIGH", "EXACT", "Standard Ayurvedic headache classification"),

                createSampleCode("NAM003", "Kaphaj Kasa", "Cough due to Kapha dosha imbalance", "Ayurveda",
                        "Respiratory disorders", "WHO-AY-003", "Kapha-predominant cough",
                        "XM3DB7", "Cough syndromes", "http://id.who.int/icd/entity/455013394",
                        "XN4C1", "Chronic cough", "http://id.who.int/icd/entity/455013395",
                        "HIGH", "EXACT", "Classical Ayurvedic respiratory condition"),

                createSampleCode("SID001", "Suram", "Fever condition in Siddha medicine", "Siddha",
                        "Fever conditions", "WHO-SI-001", "Siddha fever syndrome",
                        "XM4KH5", "Fever, unspecified", "http://id.who.int/icd/entity/455013390",
                        "XN1Z8", "Essential fever", "http://id.who.int/icd/entity/455013391",
                        "HIGH", "BROAD", "Siddha fever encompasses multiple fever types"),

                createSampleCode("UNA001", "Humma", "Fever in Unani medicine", "Unani",
                        "Febrile disorders", "WHO-UN-001", "Unani fever classification",
                        "XM4KH5", "Fever, unspecified", "http://id.who.int/icd/entity/455013390",
                        "XN1Z8", "Essential fever", "http://id.who.int/icd/entity/455013391",
                        "HIGH", "BROAD", "Unani Humma with multiple sub-classifications"),

                createSampleCode("NAM004", "Amlapitta", "Hyperacidity/GERD in Ayurveda", "Ayurveda",
                        "Digestive disorders", "WHO-AY-004", "Acid-related digestive disorder",
                        "XM6GH4", "Gastroesophageal reflux disease", "http://id.who.int/icd/entity/455013396",
                        "XN7J5", "Gastroesophageal reflux disease", "http://id.who.int/icd/entity/455013397",
                        "HIGH", "EXACT", "Classical Ayurvedic acid-peptic disorder"),

                createSampleCode("NAM005", "Sandhivata", "Osteoarthritis in Ayurveda", "Ayurveda",
                        "Joint disorders", "WHO-AY-005", "Vata-related joint degeneration",
                        "XM8FK9", "Arthrosis", "http://id.who.int/icd/entity/455013398",
                        "XN9M2", "Osteoarthritis", "http://id.who.int/icd/entity/455013399",
                        "HIGH", "EXACT", "Direct correlation with modern osteoarthritis"),

                createSampleCode("SID002", "Vatham", "Neurological disorders in Siddha", "Siddha",
                        "Neurological conditions", "WHO-SI-002", "Siddha neurological syndrome",
                        "XM1NB8", "Disorders of the nervous system", "http://id.who.int/icd/entity/455013400",
                        "XN3P7", "Neurological disorder, unspecified", "http://id.who.int/icd/entity/455013401",
                        "MEDIUM", "BROAD", "Broad Siddha neurological category"),

                createSampleCode("UNA002", "Waja-ul-Mafasil", "Joint pain in Unani medicine", "Unani",
                        "Joint disorders", "WHO-UN-002", "Unani arthralgia",
                        "XM5QW3", "Joint disorders", "http://id.who.int/icd/entity/455013402",
                        "XN6T4", "Arthralgia", "http://id.who.int/icd/entity/455013403",
                        "HIGH", "EXACT", "Unani joint pain with inflammatory component"),

                createSampleCode("NAM006", "Prameha", "Diabetes mellitus in Ayurveda", "Ayurveda",
                        "Metabolic disorders", "WHO-AY-006", "Ayurvedic diabetes classification",
                        "XM7LD6", "Diabetes mellitus", "http://id.who.int/icd/entity/455013404",
                        "XN8K3", "Type 2 diabetes mellitus", "http://id.who.int/icd/entity/455013405",
                        "HIGH", "EXACT", "Classical Ayurvedic diabetes with 20 subtypes")
        );

        namasteCodeRepository.saveAll(sampleCodes);
        log.info("Successfully loaded {} sample NAMASTE codes with dual coding mappings", sampleCodes.size());
    }

    private NamasteCode createSampleCode(String namasteCode, String namasteName, String namasteDescription,
                                         String category, String subCategory,
                                         String whoCode, String whoName,
                                         String tm2Code, String tm2Name, String tm2Uri,
                                         String biomedicineCode, String biomedicineName, String biomedicineUri,
                                         String confidence, String mappingType, String notes) {
        NamasteCode code = new NamasteCode();

        // NAMASTE fields
        code.setNamasteCode(namasteCode);
        code.setNamasteName(namasteName);
        code.setNamasteDescription(namasteDescription);
        code.setNamasteCategory(category);
        code.setNamasteSubCategory(subCategory);

        // WHO International Terminologies
        code.setWhoAyurvedaCode(whoCode);
        code.setWhoAyurvedaName(whoName);
        code.setWhoAyurvedaDescription(whoName + " - International terminology");

        // ICD-11 TM2
        code.setIcd11Tm2Code(tm2Code);
        code.setIcd11Tm2Name(tm2Name);
        code.setIcd11Tm2Description(tm2Name + " - Traditional Medicine Module 2");
        code.setIcd11Tm2Uri(tm2Uri);

        // ICD-11 Biomedicine
        code.setIcd11BiomedicineCode(biomedicineCode);
        code.setIcd11BiomedicineName(biomedicineName);
        code.setIcd11BiomedicineDescription(biomedicineName + " - Biomedicine classification");
        code.setIcd11BiomedicineUri(biomedicineUri);

        // Mapping metadata
        code.setMappingConfidence(confidence);
        code.setMappingType(mappingType);
        code.setMappingNotes(notes);
        code.setMappedBy("WHO_EXPERT_PANEL");

        // Version info
        code.setNamasteVersion("1.0.0");
        code.setIcd11Version("2024-01");
        code.setActive(true);
        code.setCreatedAt(LocalDateTime.now());
        code.setCreatedBy("DATA_LOADER");

        return code;
    }
}