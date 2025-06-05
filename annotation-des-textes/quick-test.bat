@echo off
echo ========================================
echo TEST RAPIDE DATASET DETAILS
echo ========================================
echo.

echo Redemarrage de l'application...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do (
    taskkill /F /PID %%a 2>nul
)

timeout /t 2 /nobreak >nul

echo Demarrage...
start /B mvnw.cmd spring-boot:run

echo Attente du demarrage...
:wait
timeout /t 2 /nobreak >nul
curl -s -o nul http://localhost:8081/login
if %errorlevel% neq 0 goto wait

echo.
echo Test d'acces...
set COOKIE_FILE=%TEMP%\cookies.txt
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login
curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets/details/2 > result.html

echo.
echo Verification...
findstr /i "Total Text Pairs" result.html
findstr /i "No text pairs found" result.html

echo.
echo Taille du fichier: 
for %%A in (result.html) do echo %%~zA octets

echo.
echo IMPORTANT: Verifiez la console de l'application pour les messages DEBUG
echo.

del "%COOKIE_FILE%" 2>nul
del result.html 2>nul

pause
