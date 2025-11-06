@echo off
echo === Testing Order API ===
echo.

set BASE_URL=http://localhost:8080/api/v1/orders

echo 1. Creating Order 1
curl -X POST %BASE_URL% -H "Content-Type: application/json" -d "{\"externalId\":\"ORDER-001\",\"items\":[{\"productCode\":\"PROD-001\",\"quantity\":2,\"unitPrice\":50.00},{\"productCode\":\"PROD-002\",\"quantity\":1,\"unitPrice\":30.00}]}"
echo.

echo 2. Creating Order 2
curl -X POST %BASE_URL% -H "Content-Type: application/json" -d "{\"externalId\":\"ORDER-002\",\"items\":[{\"productCode\":\"PROD-003\",\"quantity\":5,\"unitPrice\":20.00}]}"
echo.

echo 3. Testing Duplicate Order (should return 409 Conflict)
curl -X POST %BASE_URL% -H "Content-Type: application/json" -d "{\"externalId\":\"ORDER-001\",\"items\":[{\"productCode\":\"PROD-001\",\"quantity\":1,\"unitPrice\":10.00}]}"
echo.

echo 4. Get Order by External ID
curl -X GET %BASE_URL%/external/ORDER-001
echo.

echo 5. List All Orders (paginated)
curl -X GET "%BASE_URL%?page=0&size=10"
echo.

echo 6. List Orders by Status COMPLETED
curl -X GET "%BASE_URL%/status/COMPLETED?page=0&size=10"
echo.

echo === Tests Completed ===
pause

