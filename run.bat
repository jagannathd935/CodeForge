@echo off
title CodeForge Platform Launcher
color 0B

echo ========================================================
echo                 CodeForge Platform Launcher             
echo ========================================================

set "PATH=C:\Program Files\nodejs;C:\Users\Jagannath Dalei\.gemini\maven\apache-maven-3.9.6\bin;%PATH%"

echo.
echo [+] Configuration:
echo     Backend API:  http://localhost:8080
echo     Frontend UI:  http://localhost:5173
echo     H2 Console:   http://localhost:8080/h2-console
echo     Default Accounts:
echo       Admin:      admin     / admin123
echo       Organizer:  organizer / organizer123
echo       Student:    john_doe  / user123
echo ========================================================
echo.

echo [+] Launching Spring Boot Backend...
start "CodeForge Backend (Spring Boot)" cmd /k "cd backend && mvn spring-boot:run"

echo [+] Launching React Frontend (Vite)...
start "CodeForge Frontend (React Vite)" cmd /k "cd frontend && npm run dev"

echo.
echo [✔] Both services started in separate console windows!
echo     Please wait a few moments for the servers to initialize, then visit:
echo     http://localhost:5173
echo.
pause
