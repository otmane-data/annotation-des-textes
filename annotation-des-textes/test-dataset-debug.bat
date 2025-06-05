@echo off
echo ========================================
echo TEST DEBUG DATASET DETAILS
echo ========================================
echo.

echo 1. Arret des processus existants...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do (
    taskkill /F /PID %%a 2>nul
)

echo.
echo 2. Attente de 3 secondes...
timeout /t 3 /nobreak >nul

echo.
echo 3. Demarrage de l'application avec logs detailles...
start /B mvnw.cmd spring-boot:run -Dlogging.level.com.otmane.annotation_des_textes.controllers.DatasetController=DEBUG

echo.
echo 4. Attente du demarrage...
:wait_app
timeout /t 2 /nobreak >nul
curl -s -o nul http://localhost:8081/login
if %errorlevel% neq 0 (
    echo Application en cours de demarrage...
    goto wait_app
)

echo Application demarree !
echo.

echo 5. Test d'acces aux details du dataset...
set COOKIE_FILE=%TEMP%\test_cookies.txt

REM Connexion
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

REM Test d'acces aux details du dataset
echo Test des details du dataset (ID 2)...
curl -b "%COOKIE_FILE%" -s -w "Code: %%{http_code}\n" -o dataset_debug.html http://localhost:8081/admin/datasets/details/2

echo.
echo 6. Verification du resultat...
if exist dataset_debug.html (
    findstr /i "error\|exception\|500" dataset_debug.html >nul
    if %errorlevel% equ 0 (
        echo ❌ ERREUR DETECTEE
        findstr /i "error\|exception\|500" dataset_debug.html
    ) else (
        echo ✅ SUCCES - Page chargee sans erreur
        echo.
        echo Verification du contenu...
        findstr /i "Total Text Pairs" dataset_debug.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Section Total Text Pairs trouvee
        ) else (
            echo ⚠️ Section Total Text Pairs non trouvee
        )
        
        findstr /i "No text pairs found" dataset_debug.html >nul
        if %errorlevel% equ 0 (
            echo ⚠️ Message "No text pairs found" detecte
        ) else (
            echo ✅ Pas de message "No text pairs found"
        )
    )
    
    echo.
    echo Taille du fichier:
    for %%A in (dataset_debug.html) do echo %%~zA octets
    
    echo.
    echo Premiers 500 caracteres de la reponse:
    powershell -command "Get-Content dataset_debug.html -Raw | ForEach-Object { $_.Substring(0, [Math]::Min(500, $_.Length)) }"
) else (
    echo ❌ Fichier de reponse non trouve
)

echo.
echo 7. Verification des logs de l'application...
echo Recherche des logs dans la console...
echo (Les logs detailles devraient apparaitre dans la console de l'application)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del dataset_debug.html 2>nul

echo ========================================
echo FIN DU TEST DEBUG
echo ========================================
echo.
echo IMPORTANT: Verifiez les logs dans la console de l'application
echo pour voir les messages de debug du DatasetController
echo.
pause
