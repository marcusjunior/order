@echo off
echo ====================================
echo   Order Management System
echo   Complete Setup and Demo
echo ====================================
echo.

echo [1/5] Starting Docker services...
docker-compose up -d
if %errorlevel% neq 0 (
    echo ERROR: Failed to start Docker services
    pause
    exit /b 1
)
echo Docker services started successfully!
echo.

echo Waiting for services to be ready...
timeout /t 10 /nobreak >nul

echo [2/5] Checking service health...
docker ps
echo.

echo [3/5] Building application...
call gradlew.bat clean build -x test
if %errorlevel% neq 0 (
    echo ERROR: Build failed
    pause
    exit /b 1
)
echo Build successful!
echo.

echo [4/5] Starting application...
echo The application will start in a new window...
echo.
start "Order API" cmd /c "gradlew.bat bootRun"

echo Waiting for application to start...
timeout /t 20 /nobreak >nul

echo [5/5] Testing API...
echo.
echo Creating test order...
curl -X POST http://localhost:8080/api/v1/orders -H "Content-Type: application/json" -d "{\"externalId\":\"SETUP-TEST-001\",\"items\":[{\"productCode\":\"PROD-001\",\"quantity\":2,\"unitPrice\":50.0}]}"
echo.
echo.

echo ====================================
echo   Setup Complete!
echo ====================================
echo.
echo Application: http://localhost:8080
echo RabbitMQ: http://localhost:15672 (guest/guest)
echo Health: http://localhost:8080/actuator/health
echo.
echo Run 'test-api.bat' to execute more tests
echo Run 'docker-compose down' to stop services
echo.
pause

