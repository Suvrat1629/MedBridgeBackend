@echo off
REM NAMASTE-TM2 FHIR Terminology Service - Windows Testing Script
REM Usage: test-endpoints.bat
REM Make sure the application is running on http://localhost:8082

set BASE_URL=http://localhost:8082
echo 🧪 Testing NAMASTE-TM2 FHIR Terminology Service
echo 📍 Base URL: %BASE_URL%
echo ----------------------------------------

echo.
echo 🏥 1. HEALTH CHECK ENDPOINTS
echo.

echo 🔹 Testing: Terminology Service Health
curl -X GET "%BASE_URL%/api/terminology/health"
echo.
echo ----------------------------------------

echo 🔹 Testing: FHIR Service Health
curl -X GET "%BASE_URL%/api/fhir/health"
echo.
echo ----------------------------------------

echo.
echo 🔍 2. TERMINOLOGY SEARCH ^& MAPPING
echo.

echo 🔹 Testing: Auto-Complete Search
curl -X GET "%BASE_URL%/api/terminology/autocomplete?query=fever&limit=3"
echo.
echo ----------------------------------------

echo 🔹 Testing: Get Details by NAMASTE Name
curl -X GET "%BASE_URL%/api/terminology/name/Vataj%%20Jwara"
echo.
echo ----------------------------------------

echo 🔹 Testing: NAMASTE to ICD-11 TM2 Translation
curl -X GET "%BASE_URL%/api/terminology/translate/tm2/NAM001"
echo.
echo ----------------------------------------

echo 🔹 Testing: NAMASTE to ICD-11 Biomedicine Translation
curl -X GET "%BASE_URL%/api/terminology/translate/biomedicine/NAM001"
echo.
echo ----------------------------------------

echo 🔹 Testing: Terminology Statistics
curl -X GET "%BASE_URL%/api/terminology/stats"
echo.
echo ----------------------------------------

echo.
echo 🧬 3. FHIR RESOURCE CREATION
echo.

echo 🔹 Testing: Create FHIR Condition (Problem List)
curl -X POST "%BASE_URL%/api/fhir/problem-list" -H "Content-Type: application/json" -d "{\"patientId\": \"patient-123\", \"namasteCode\": \"NAM001\"}"
echo.
echo ----------------------------------------

echo 🔹 Testing: Create FHIR Encounter Bundle
curl -X POST "%BASE_URL%/api/fhir/encounter-bundle" -H "Content-Type: application/json" -d "{\"patientId\": \"patient-123\", \"encounterId\": \"encounter-456\", \"namasteCodes\": [\"NAM001\", \"NAM002\"]}"
echo.
echo ----------------------------------------

echo 🔹 Testing: Get Sample FHIR Bundle
curl -X GET "%BASE_URL%/api/fhir/sample-bundle"
echo.
echo ----------------------------------------

echo 🔹 Testing: Get NAMASTE FHIR CodeSystem
curl -X GET "%BASE_URL%/api/fhir/codesystem/namaste"
echo.
echo ----------------------------------------

echo.
echo 📦 4. FHIR BUNDLE PROCESSING
echo.

echo 🔹 Testing: Upload ^& Process FHIR Bundle
curl -X POST "%BASE_URL%/api/fhir/upload-bundle" -H "Content-Type: application/json" -d "{\"resourceType\": \"Bundle\", \"type\": \"transaction\", \"entry\": [{\"resource\": {\"resourceType\": \"Condition\", \"id\": \"test-condition-1\", \"code\": {\"coding\": [{\"system\": \"http://terminology.hl7.org.in/CodeSystem/namaste\", \"code\": \"NAM001\", \"display\": \"Vataj Jwara\"}]}}}]}"
echo.
echo ----------------------------------------

echo 🔹 Testing: Dual-Coded Disease Concept
curl -X POST "%BASE_URL%/api/fhir/code-disease" -H "Content-Type: application/json" -d "{\"diseaseName\": \"Vataj Jwara\"}"
echo.
echo ----------------------------------------

echo.
echo 🎯 5. ADVANCED TESTS
echo.

echo 🔹 Testing: Search by Category
curl -X GET "%BASE_URL%/api/terminology/category/Ayurveda"
echo.
echo ----------------------------------------

echo 🔹 Testing: Dual Coding Records
curl -X GET "%BASE_URL%/api/terminology/dual-coding"
echo.
echo ----------------------------------------

echo 🔹 Testing: Comprehensive Search
curl -X GET "%BASE_URL%/api/terminology/search?query=jwara"
echo.
echo ----------------------------------------

echo.
echo 📊 TESTING SUMMARY
echo ----------------------------------------
echo ✅ If all endpoints returned JSON responses, your FHIR terminology service is working!
echo.
echo 🔗 Next Steps:
echo 1. Open H2 Console: http://localhost:8082/h2-console
echo 2. Check sample data: SELECT * FROM namaste_codes;
echo 3. Test with Postman using the requests above
echo 4. Integrate with your EMR frontend
echo.
echo 💡 Note: Install 'jq' for better JSON formatting: curl ... ^| jq .
echo.
pause