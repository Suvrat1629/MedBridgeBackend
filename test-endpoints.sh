#!/bin/bash

# NAMASTE-TM2 FHIR Terminology Service - Endpoint Testing Script
# Usage: ./test-endpoints.sh
# Make sure the application is running on http://localhost:8082

BASE_URL="http://localhost:8082"
echo "🧪 Testing NAMASTE-TM2 FHIR Terminology Service"
echo "📍 Base URL: $BASE_URL"
echo "----------------------------------------"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test function
test_endpoint() {
    local test_name="$1"
    local curl_command="$2"
    local expected_status="$3"
    
    echo -e "${BLUE}🔹 Testing: $test_name${NC}"
    
    response=$(eval "$curl_command" -w "HTTP_STATUS:%{http_code}" -s)
    http_status=$(echo "$response" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed 's/HTTP_STATUS:[0-9]*$//')
    
    if [[ "$http_status" -eq "$expected_status" ]]; then
        echo -e "${GREEN}✅ SUCCESS - HTTP $http_status${NC}"
    else
        echo -e "${RED}❌ FAILED - Expected HTTP $expected_status, got $http_status${NC}"
    fi
    
    # Pretty print JSON response (first 500 chars)
    if [[ ${#response_body} -gt 500 ]]; then
        echo "${response_body:0:500}..." | jq . 2>/dev/null || echo "${response_body:0:500}..."
    else
        echo "$response_body" | jq . 2>/dev/null || echo "$response_body"
    fi
    
    echo "----------------------------------------"
}

echo "🏥 1. HEALTH CHECK ENDPOINTS"

test_endpoint "Terminology Service Health" \
    "curl -X GET '$BASE_URL/api/terminology/health'" \
    200

test_endpoint "FHIR Service Health" \
    "curl -X GET '$BASE_URL/api/fhir/health'" \
    200

echo ""
echo "🔍 2. TERMINOLOGY SEARCH & MAPPING"

test_endpoint "Auto-Complete Search" \
    "curl -X GET '$BASE_URL/api/terminology/autocomplete?query=fever&limit=3'" \
    200

test_endpoint "Get Details by NAMASTE Name" \
    "curl -X GET '$BASE_URL/api/terminology/name/Vataj%20Jwara'" \
    200

test_endpoint "NAMASTE to ICD-11 TM2 Translation" \
    "curl -X GET '$BASE_URL/api/terminology/translate/tm2/NAM001'" \
    200

test_endpoint "NAMASTE to ICD-11 Biomedicine Translation" \
    "curl -X GET '$BASE_URL/api/terminology/translate/biomedicine/NAM001'" \
    200

test_endpoint "Terminology Statistics" \
    "curl -X GET '$BASE_URL/api/terminology/stats'" \
    200

echo ""
echo "🧬 3. FHIR RESOURCE CREATION"

test_endpoint "Create FHIR Condition (Problem List)" \
    "curl -X POST '$BASE_URL/api/fhir/problem-list' -H 'Content-Type: application/json' -d '{\"patientId\": \"patient-123\", \"namasteCode\": \"NAM001\"}'" \
    200

test_endpoint "Create FHIR Encounter Bundle" \
    "curl -X POST '$BASE_URL/api/fhir/encounter-bundle' -H 'Content-Type: application/json' -d '{\"patientId\": \"patient-123\", \"encounterId\": \"encounter-456\", \"namasteCodes\": [\"NAM001\", \"NAM002\"]}'" \
    200

test_endpoint "Get Sample FHIR Bundle" \
    "curl -X GET '$BASE_URL/api/fhir/sample-bundle'" \
    200

test_endpoint "Get NAMASTE FHIR CodeSystem" \
    "curl -X GET '$BASE_URL/api/fhir/codesystem/namaste'" \
    200

echo ""
echo "📦 4. FHIR BUNDLE PROCESSING"

# Create a test FHIR bundle for upload
test_bundle='{
  "resourceType": "Bundle",
  "type": "transaction",
  "entry": [
    {
      "resource": {
        "resourceType": "Condition",
        "id": "test-condition-1",
        "code": {
          "coding": [
            {
              "system": "http://terminology.hl7.org.in/CodeSystem/namaste",
              "code": "NAM001",
              "display": "Vataj Jwara"
            }
          ]
        }
      }
    }
  ]
}'

test_endpoint "Upload & Process FHIR Bundle" \
    "curl -X POST '$BASE_URL/api/fhir/upload-bundle' -H 'Content-Type: application/json' -d '$test_bundle'" \
    200

test_endpoint "Validate FHIR Bundle" \
    "curl -X POST '$BASE_URL/api/fhir/validate-bundle' -H 'Content-Type: application/json' -d '$test_bundle'" \
    200

echo ""
echo "🔐 5. ABHA AUTHENTICATION (Sample)"

test_endpoint "ABHA Service Health Check" \
    "curl -X GET '$BASE_URL/api/abha/check/sample-health-id'" \
    200

echo ""
echo "🎯 6. ADVANCED TESTS"

test_endpoint "Search by Category" \
    "curl -X GET '$BASE_URL/api/terminology/category/Ayurveda'" \
    200

test_endpoint "Dual Coding Records" \
    "curl -X GET '$BASE_URL/api/terminology/dual-coding'" \
    200

test_endpoint "Comprehensive Search" \
    "curl -X GET '$BASE_URL/api/terminology/search?query=jwara'" \
    200

echo ""
echo "📊 TESTING SUMMARY"
echo "----------------------------------------"
echo -e "${GREEN}✅ If all tests show SUCCESS, your FHIR terminology service is working correctly!${NC}"
echo ""
echo "🔗 Next Steps:"
echo "1. Open H2 Console: http://localhost:8082/h2-console"
echo "2. Check sample data: SELECT * FROM namaste_codes;"
echo "3. Test with Postman using the requests above"
echo "4. Integrate with your EMR frontend"
echo ""
echo -e "${YELLOW}💡 Tip: Use 'jq' for better JSON formatting: curl ... | jq .${NC}"