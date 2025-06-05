@echo off
echo ========================================
echo TEST COMPLET PARSING CSV
echo ========================================
echo.

echo 1. Creation du fichier CSV de test...
echo text1,text2 > test_dataset.csv
echo "Hello World","Bonjour Monde" >> test_dataset.csv
echo "Good morning","Bon matin" >> test_dataset.csv
echo "How are you?","Comment allez-vous?" >> test_dataset.csv
echo "Thank you","Merci" >> test_dataset.csv
echo "Goodbye","Au revoir" >> test_dataset.csv

echo ✅ Fichier CSV cree avec 5 couples de texte
echo.

echo 2. Arret des processus existants...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do (
    taskkill /F /PID %%a 2>nul
)

echo.
echo 3. Compilation...
call mvnw.cmd compile
if %errorlevel% neq 0 (
    echo ❌ ERREUR DE COMPILATION
    pause
    exit /b 1
)

echo ✅ Compilation reussie !
echo.

echo 4. Demarrage de l'application...
start /B mvnw.cmd spring-boot:run

echo.
echo 5. Attente du demarrage...
:wait_app
timeout /t 2 /nobreak >nul
curl -s -o nul http://localhost:8081/login
if %errorlevel% neq 0 (
    echo Application en cours de demarrage...
    goto wait_app
)

echo ✅ Application demarree !
echo.

echo 6. Test de creation avec CSV...
set COOKIE_FILE=%TEMP%\test_cookies.txt

REM Connexion
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo Test de soumission du dataset CSV...
curl -b "%COOKIE_FILE%" ^
     -F "name=Test Dataset CSV" ^
     -F "description=Test de parsing CSV" ^
     -F "classes=positive;negative" ^
     -F "file=@test_dataset.csv" ^
     -v ^
     -L ^
     -o creation_result.html ^
     http://localhost:8081/admin/datasets/save

echo.
echo 7. Attente du parsing asynchrone (10 secondes)...
timeout /t 10 /nobreak >nul

echo.
echo 8. Verification du resultat...
curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets/details/1 > dataset_details.html

findstr /i "Total Text Pairs" dataset_details.html >nul
if %errorlevel% equ 0 (
    echo ✅ Section Total Text Pairs trouvee
    findstr /i "Total Text Pairs" dataset_details.html
) else (
    echo ⚠️ Section Total Text Pairs non trouvee
)

findstr /i "No text pairs found" dataset_details.html >nul
if %errorlevel% equ 0 (
    echo ⚠️ Message "No text pairs found" detecte
) else (
    echo ✅ Pas de message d'absence de text pairs
)

echo.
echo 9. Verification des erreurs...
curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets > datasets_list.html

findstr /i "error\|Error" datasets_list.html >nul
if %errorlevel% equ 0 (
    echo ⚠️ Erreurs detectees:
    findstr /i "error\|Error" datasets_list.html
) else (
    echo ✅ Aucune erreur detectee sur la page datasets
)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del creation_result.html 2>nul
del dataset_details.html 2>nul
del datasets_list.html 2>nul

echo ========================================
echo RESULTATS DU TEST
echo ========================================
echo.
echo IMPORTANT: Verifiez dans la console de l'application:
echo 1. "Starting async parsing for dataset: Test Dataset CSV"
echo 2. "Parsing CSV file..."
echo 3. "Successfully parsed 5 rows from dataset file"
echo 4. "Successfully completed async parsing for dataset..."
echo.
echo Si vous voyez ces messages, le parsing CSV fonctionne !
echo Si vous voyez des erreurs, elles seront detaillees dans les logs.
echo.
pause
