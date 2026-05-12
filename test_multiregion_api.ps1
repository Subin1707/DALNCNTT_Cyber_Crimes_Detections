# Test MultiRegion API Endpoints
# Run after server starts: mvn spring-boot:run

Write-Host "=== MultiRegion API Testing ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/multiregion"
$delay = 3

# Test 1: Get regions
Write-Host "`n[1] GET /api/multiregion/regions" -ForegroundColor Yellow
Start-Sleep -Seconds $delay
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/regions" -UseBasicParsing
    $json = $response.Content | ConvertFrom-Json
    Write-Host "✓ Response: " -ForegroundColor Green
    $json | ConvertTo-Json -Depth 3 | Write-Host
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Demo three regions (triggers console output)
Write-Host "`n[2] GET /api/multiregion/demo/three-regions" -ForegroundColor Yellow
Start-Sleep -Seconds $delay
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/demo/three-regions" -UseBasicParsing
    $json = $response.Content | ConvertFrom-Json
    Write-Host "✓ Response: " -ForegroundColor Green
    $json | ConvertTo-Json -Depth 3 | Write-Host
    Write-Host "   (Check server console for detailed output)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Safe node
Write-Host "`n[3] GET /api/multiregion/sample-nodes/safe" -ForegroundColor Yellow
Start-Sleep -Seconds $delay
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/sample-nodes/safe" -UseBasicParsing
    $json = $response.Content | ConvertFrom-Json
    Write-Host "✓ Response: " -ForegroundColor Green
    $json | ConvertTo-Json -Depth 3 | Write-Host
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Suspicious node
Write-Host "`n[4] GET /api/multiregion/sample-nodes/suspicious" -ForegroundColor Yellow
Start-Sleep -Seconds $delay
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/sample-nodes/suspicious" -UseBasicParsing
    $json = $response.Content | ConvertFrom-Json
    Write-Host "✓ Response: " -ForegroundColor Green
    $json | ConvertTo-Json -Depth 3 | Write-Host
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Fraud node
Write-Host "`n[5] GET /api/multiregion/sample-nodes/fraud" -ForegroundColor Yellow
Start-Sleep -Seconds $delay
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/sample-nodes/fraud" -UseBasicParsing
    $json = $response.Content | ConvertFrom-Json
    Write-Host "✓ Response: " -ForegroundColor Green
    $json | ConvertTo-Json -Depth 3 | Write-Host
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Custom node analysis (POST)
Write-Host "`n[6] POST /api/multiregion/analyze (custom node)" -ForegroundColor Yellow
Start-Sleep -Seconds $delay
$customNode = @{
    ipCount = 8
    urlCount = 12
    emailCount = 10
    domainCount = 6
    failedLoginCount = 2
    requestFrequency = 3.0
    vpn = $true
    blacklist = $false
    suspiciousUrl = $true
    torNetwork = $false
    spamPattern = $true
    abnormalAccessTime = $true
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/analyze" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body $customNode `
        -UseBasicParsing
    $json = $response.Content | ConvertFrom-Json
    Write-Host "✓ Response: " -ForegroundColor Green
    $json | ConvertTo-Json -Depth 3 | Write-Host
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Testing Complete ===" -ForegroundColor Cyan
