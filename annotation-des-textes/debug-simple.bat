@echo off
echo ========================================
echo DEBUG SIMPLE - DIAGNOSTIC TEXT PAIRS
echo ========================================
echo.

echo 1. Creation d'un fichier CSV minimal...
echo text1,text2 > minimal.csv
echo Hello,World >> minimal.csv
echo Good,Bad >> minimal.csv

echo ✅ Fichier minimal.csv cree
echo Contenu:
type minimal.csv
echo.

echo 2. Test de l'endpoint de debug...
curl -s http://localhost:8081/login >nul
if %errorlevel% neq 0 (
    echo ❌ Application non accessible
    pause
    exit /b 1
)

echo ✅ Application accessible
echo.

echo 3. Connexion et test de debug pour dataset existant...
set COOKIE_FILE=%TEMP%\debug_cookies.txt

curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo Test de debug pour dataset ID 1:
curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets/1/debug

echo.
echo 4. Creation d'un nouveau dataset pour test...
curl -b "%COOKIE_FILE%" ^
     -F "name=Debug Test" ^
     -F "description=Test de debug" ^
     -F "classes=positive;negative" ^
     -F "file=@minimal.csv" ^
     -s ^
     http://localhost:8081/admin/datasets/save

echo.
echo 5. Attente du parsing (10 secondes)...
timeout /t 10 /nobreak >nul

echo.
echo 6. Debug du nouveau dataset...
echo Test de debug pour le dernier dataset cree:
curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets/2/debug

echo.
echo 7. Verification manuelle recommandee...
echo Allez sur http://localhost:8081/admin/datasets/1/debug
echo ou http://localhost:8081/admin/datasets/2/debug
echo pour voir les informations de debug detaillees.

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul

echo ========================================
echo INSTRUCTIONS
echo ========================================
echo.
echo 1. Verifiez les logs de l'application pour:
echo    - Messages "Starting async parsing..."
echo    - Messages "DEBUG: Saving batch..."
echo    - Messages "DEBUG: Successfully saved..."
echo.
echo 2. Testez l'endpoint de debug:
echo    http://localhost:8081/admin/datasets/1/debug
echo.
echo 3. Verifiez les informations affichees:
echo    - Dataset found: [nom]
echo    - File exists: true
echo    - Total couples in DB: [nombre]
echo    - Couples found by findAll: [nombre]
echo    - Couples found by pagination: [nombre]
echo.
echo Si "Total couples in DB" = 0, le probleme est dans le parsing/sauvegarde
echo Si "Total couples in DB" > 0 mais pagination = 0, probleme dans les requetes
echo.
pause
