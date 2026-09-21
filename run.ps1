# =====================================================================
# CodeForge: Master Project Launcher (PowerShell)
# Description: Starts Spring Boot backend on 8080 & Vite React frontend on 5173
# =====================================================================

$ErrorActionPreference = "Stop"

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "                CodeForge Platform Launcher             " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

# Setup environment PATH
$nodePath = "C:\Program Files\nodejs"
$mavenPath = "C:\Users\Jagannath Dalei\.gemini\maven\apache-maven-3.9.6\bin"
$env:Path = "$nodePath;$mavenPath;$env:Path"

Write-Host "[+] Verifying Environment..." -ForegroundColor Yellow
try {
    $javaVer = (& java -version 2>&1 | Select-Object -First 1)
    Write-Host "    Java:   $javaVer" -ForegroundColor Green
    $nodeVer = (& node -v)
    Write-Host "    Node:   $nodeVer" -ForegroundColor Green
    $npmVer = (& npm -v)
    Write-Host "    npm:    $npmVer" -ForegroundColor Green
    $mvnVer = (& mvn -v | Select-Object -First 1)
    Write-Host "    Maven:  $mvnVer" -ForegroundColor Green
} catch {
    Write-Host "[-] Missing required runtime tools: $_" -ForegroundColor Red
}

Write-Host "`n[+] Configuration:" -ForegroundColor Yellow
Write-Host "    Backend API:  http://localhost:8080" -ForegroundColor Cyan
Write-Host "    Frontend UI:  http://localhost:5173" -ForegroundColor Cyan
Write-Host "    H2 Console:   http://localhost:8080/h2-console" -ForegroundColor Cyan
Write-Host "    Credentials:" -ForegroundColor Cyan
Write-Host "      Admin:      admin     / admin123" -ForegroundColor White
Write-Host "      Organizer:  organizer / organizer123" -ForegroundColor White
Write-Host "      Student:    john_doe  / user123" -ForegroundColor White
Write-Host "========================================================`n" -ForegroundColor Cyan

$backendDir = Join-Path $PSScriptRoot "backend"
$frontendDir = Join-Path $PSScriptRoot "frontend"

Write-Host "[+] Launching Spring Boot Backend in background..." -ForegroundColor Green
$backendProc = Start-Process -FilePath "mvn.cmd" -ArgumentList "spring-boot:run" -WorkingDirectory $backendDir -PassThru

Write-Host "[+] Launching React Vite Frontend..." -ForegroundColor Green
Start-Process -FilePath "npm.cmd" -ArgumentList "run", "dev" -WorkingDirectory $frontendDir

Write-Host "`n[✔] Both services launched successfully!" -ForegroundColor Green
Write-Host "    Open http://localhost:5173 in your browser to begin practicing coding." -ForegroundColor Yellow
Write-Host "    Backend PID: $($backendProc.Id)" -ForegroundColor Gray
