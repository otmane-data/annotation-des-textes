@echo off
echo ========================================
echo TEST DE COMPILATION ET FONCTIONNEMENT
echo ========================================
echo.

echo 1. Arret des processus existants...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do (
    taskkill /F /PID %%a 2>nul
)

echo.
echo 2. Test de compilation...
call mvnw.cmd compile
if %errorlevel% neq 0 (
    echo ❌ ERREUR DE COMPILATION
    pause
    exit /b 1
)

echo ✅ Compilation reussie !
echo.

echo 3. Demarrage de l'application...
start /B mvnw.cmd spring-boot:run

echo.
echo 4. Attente du demarrage...
:wait_app
timeout /t 2 /nobreak >nul
curl -s -o nul http://localhost:8081/login
if %errorlevel% neq 0 (
    echo Application en cours de demarrage...
    goto wait_app
)

echo ✅ Application demarree !
echo.

echo 5. Test d'acces aux datasets...
set COOKIE_FILE=%TEMP%\test_cookies.txt

REM Connexion
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

REM Test d'acces aux details du dataset
echo Test des details du dataset (ID 2)...
curl -b "%COOKIE_FILE%" -s -w "Code HTTP: %%{http_code}\n" -o test_result.html http://localhost:8081/admin/datasets/details/2

echo.
echo 6. Verification du resultat...
if exist test_result.html (
    findstr /i "error\|exception\|500" test_result.html >nul
    if %errorlevel% equ 0 (
        echo ❌ ERREUR DETECTEE dans la reponse
    ) else (
        echo ✅ SUCCES - Aucune erreur detectee
        
        echo.
        echo Verification du contenu...
        findstr /i "Total Text Pairs" test_result.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Section "Total Text Pairs" trouvee
        ) else (
            echo ⚠️ Section "Total Text Pairs" non trouvee
        )
        
        findstr /i "No text pairs found" test_result.html >nul
        if %errorlevel% equ 0 (
            echo ⚠️ Message "No text pairs found" detecte
        ) else (
            echo ✅ Pas de message d'absence de text pairs
        )
    )
    
    echo.
    echo Taille du fichier de reponse:
    for %%A in (test_result.html) do echo %%~zA octets
) else (
    echo ❌ Fichier de reponse non trouve
)

echo.
echo 7. Verification des logs...
echo IMPORTANT: Verifiez la console de l'application pour les messages DEBUG
echo qui commencent par "DEBUG: getCoupleTextsByDatasetId" et "DEBUG: countCoupleTextsByDatasetId"

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del test_result.html 2>nul

echo ========================================
echo FIN DU TEST
echo ========================================
echo.
echo Si vous voyez "✅ SUCCES" ci-dessus, l'application fonctionne.
echo Verifiez les logs dans la console pour diagnostiquer le probleme des text pairs.
echo.
pause
