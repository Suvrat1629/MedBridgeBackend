# NAMASTE-TM2 FHIR Terminology Service

A **FHIR R4-compliant Traditional Medicine to ICD-11 TM2 mapping service** built with Spring Boot and MongoDB. Integrates traditional Indian medicine codes with WHO ICD-11 Traditional Medicine Module 2 for healthcare interoperability.

## 🎯 **Core Features**

✅ **MongoDB-based terminology storage** with TM2 mappings  
✅ **FHIR R4 compliance** using HAPI FHIR  
✅ **Traditional Medicine code search** (NAMASTE/Ayurveda/Siddha/Unani)  
✅ **Symptom-based search** with fuzzy matching  
✅ **ABHA authentication** integration  
✅ **Auto-complete API** for EMR systems  
✅ **Confidence-based mapping** scoring

---

## 🚀 **Quick Start**

### **Prerequisites**
- Java 17+
- MongoDB (local or Atlas)
- Maven 3.6+

### **Setup & Run**
```bash
git clone <repository>
cd MedBridgeBackend
mvn spring-boot:run
```

**Application URL**: http://localhost:8082  
**MongoDB**: Configured via `application.properties`

---

## 📋 **API Endpoints**

### **🔹 Core Terminology Search**

#### **Search by Code (Primary)**
```bash
# Search in both tm2_code and code fields
GET /api/fhir/search/code/{codeValue}

# Search only TM2 codes
GET /api/fhir/search/tm2code/{codeValue}

# Search only traditional medicine codes
GET /api/fhir/search/codeonly/{codeValue}
```

#### **Symptom-based Search**
```bash
# Search by symptoms/descriptions
GET /api/fhir/search/symptoms?query=fever
```

### **🔹 ABHA Authentication**
```bash
POST /api/abha/initialize        # Initialize ABHA flow
GET  /api/abha/check/{healthId}  # Check health ID
POST /api/abha/login            # ABHA login
GET  /api/abha/profile          # Get ABHA profile
```

### **🔹 Health Checks**
```bash
GET /api/terminology/health     # Service status
GET /api/fhir/health           # FHIR service status
```

---

## 💾 **Data Model**

### **MongoDB Collection: `tm2_mappings`**

```javascript
{
  "_id": "...",
  "tm2_code": "XM4KH5",           // ICD-11 TM2 code
  "tm2_link": "...",              // TM2 URI
  "code": "NAM001",               // Traditional medicine code
  "tm2_title": "Fever, unspecified",
  "tm2_definition": "...",
  "code_title": "Vataj Jwara",    // Traditional medicine name
  "code_description": "...",      // Description
  "confidence_score": 0.85,       // Mapping confidence (0-1)
  "type": "Ayurveda"              // Medicine category
}
```

---

## 🧪 **Testing**

### **Run Automated Tests**
```bash
# Linux/Mac
./test-endpoints.sh

# Windows
./test-endpoints.bat
```

### **Sample Test Calls**
```bash
# FHIR code search
curl "http://localhost:8082/api/fhir/search/code/NAM001"

# Symptom search
curl "http://localhost:8082/api/fhir/search/symptoms?query=fever"

# Health check
curl "http://localhost:8082/api/fhir/health"
```

### **Expected Response Format (FHIR)**
```json
{
  "resourceType": "Parameters",
  "meta": {
    "versionId": "1",
    "profile": ["http://hl7.org.in/fhir/StructureDefinition/AyushParameters"]
  },
  "parameter": [
    {
      "name": "code",
      "valueString": "NAM001"
    },
    {
      "name": "display",
      "valueString": "Vataj Jwara"
    },
    {
      "name": "tm2_code", 
      "valueString": "XM4KH5"
    }
  ]
}
```

---

## ⚙️ **Configuration**

### **MongoDB Connection**
```properties
# application.properties
spring.data.mongodb.uri=mongodb+srv://user:pass@cluster.mongodb.net/traditional_medicine_db
```

### **ABHA Settings**
```properties
abha.base-url=https://sandbox.abdm.gov.in
abha.client-id=your_client_id
abha.client-secret=your_client_secret
```

### **FHIR Configuration**
```properties
fhir.terminology.version=1.0.0
fhir.codesystem.count=4500
```

---

## 🏗️ **Architecture**

- **Backend**: Spring Boot 3.5.5
- **Database**: MongoDB with Spring Data
- **FHIR**: HAPI FHIR R4 (v6.10.5)
- **Security**: OAuth2 + ABHA integration
- **Authentication**: Spring Security

### **Key Components**
- `NamasteTerminologyController` - REST endpoints (commented out)
- `FhirBundleController` - Active FHIR endpoints
- `NamasteTerminologyService` - Core business logic
- `TerminologyFhirService` - FHIR resource handling
- `NamasteCodeRepository` - MongoDB data access

---

## 🎯 **Integration Examples**

### **JavaScript Frontend**
```javascript
// Search by code
const searchCode = async (code) => {
  const response = await fetch(`/api/fhir/search/code/${code}`, {
    headers: {'Accept': 'application/fhir+json;fhirVersion=4.0'}
  });
  return await response.json(); // FHIR Parameters
};

// Symptom search
const searchSymptoms = async (query) => {
  const response = await fetch(`/api/fhir/search/symptoms?query=${query}`);
  return await response.json(); // FHIR Bundle
};
```

### **HAPI FHIR Client**
```java
FhirContext ctx = FhirContext.forR4();
IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8082/api/fhir");

// Search by code
Parameters result = client.operation()
    .onType(CodeSystem.class)
    .named("search")
    .withParameter(Parameters.class, "code", new StringType("NAM001"))
    .execute();
```

---

## 📊 **MongoDB Queries**

### **Sample Repository Methods**
```java
// Exact code search (both fields)
@Query("{'$or': [{'tm2_code': ?0}, {'code': ?0}]}")
Optional<List<NamasteCode>> findByAnyCode(String code);

// Symptom fuzzy search
@Query("{'$or': [{'code_description': {$regex: ?0, $options: 'i'}}, 
                 {'tm2_definition': {$regex: ?0, $options: 'i'}}]}")
List<NamasteCode> findBySymptoms(String query);

// High confidence mappings
@Query("{'confidence_score': {$gte: 0.8}}")
List<NamasteCode> findHighConfidenceMappings();
```

---

## 🔐 **Security Features**

- **ABHA OAuth2** integration for Indian healthcare
- **Spring Security** configuration
- **FHIR metadata** compliance with Indian EHR standards
- **Confidentiality tags** on FHIR resources

---

## 🎯 **Use Cases**

1. **EMR Integration**: Search traditional medicine codes for clinical documentation
2. **ICD-11 Mapping**: Convert traditional codes to WHO ICD-11 TM2
3. **Symptom Search**: Find codes based on patient symptoms/descriptions
4. **ABHA Authentication**: Secure access using Indian health ID system
5. **FHIR Compliance**: Standardized healthcare data exchange

---

## 🚀 **Development Status**

- ✅ **Core MongoDB integration** working
- ✅ **FHIR endpoints** active
- ✅ **ABHA authentication** implemented
- ✅ **Terminology search** functional
- ⚠️ **REST endpoints** (commented out in controller)
- ✅ **Test scripts** available

---

**Built for Indian Healthcare** | **FHIR R4 Compliant** | **Traditional Medicine Ready**
