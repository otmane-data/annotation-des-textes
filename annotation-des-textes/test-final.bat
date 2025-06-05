@echo off
echo ========================================
echo TEST FINAL - CREATION DATASET AVEC PARSING
echo ========================================
echo.

echo 1. Creation d'un fichier CSV de test...
echo text1,text2 > test_final.csv
echo "Hello World","Bonjour Monde" >> test_final.csv
echo "Good morning","Bon matin" >> test_final.csv
echo "How are you?","Comment allez-vous?" >> test_final.csv
echo "Thank you","Merci" >> test_final.csv
echo "Goodbye","Au revoir" >> test_final.csv

echo ✅ Fichier test_final.csv cree avec 5 couples de texte
echo.
echo Contenu du fichier:
type test_final.csv
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

echo 4. Demarrage de l'application avec @EnableAsync...
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

echo 6. Test de creation de dataset...
set COOKIE_FILE=%TEMP%\test_final_cookies.txt

REM Connexion
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo Creation du dataset avec parsing asynchrone...
curl -b "%COOKIE_FILE%" ^
     -F "name=Test Final Dataset" ^
     -F "description=Test final avec parsing asynchrone" ^
     -F "classes=positive;negative;neutral" ^
     -F "file=@test_final.csv" ^
     -s ^
     -o creation_result.html ^
     http://localhost:8081/admin/datasets/save

echo.
echo 7. Verification immediate (dataset cree)...
curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets > datasets_immediate.html

findstr /i "Test Final Dataset" datasets_immediate.html >nul
if %errorlevel% equ 0 (
    echo ✅ Dataset cree et visible dans la liste
) else (
    echo ❌ Dataset non trouve dans la liste
)

echo.
echo 8. Attente du parsing asynchrone (10 secondes)...
echo IMPORTANT: Surveillez la console de l'application pour les messages:
echo - "Starting async parsing for dataset: Test Final Dataset"
echo - "Parsing CSV file..."
echo - "Successfully parsed 5 rows from dataset file"
timeout /t 10 /nobreak >nul

echo.
echo 9. Verification des text pairs...
curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets/details/1 > details_final.html

echo Recherche du nombre de text pairs...
findstr /i "Total Text Pairs" details_final.html
echo.

findstr /i "No text pairs found" details_final.html >nul
if %errorlevel% equ 0 (
    echo ⚠️ Aucun text pair trouve - Test du parsing manuel...
    
    echo Declenchement du parsing synchrone...
    curl -b "%COOKIE_FILE%" -s -o sync_result.html http://localhost:8081/admin/datasets/1/parse-sync
    
    timeout /t 3 /nobreak >nul
    
    curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets/details/1 > details_after_sync.html
    
    echo Resultat apres parsing manuel:
    findstr /i "Total Text Pairs" details_after_sync.html
    
    findstr /i "No text pairs found" details_after_sync.html >nul
    if %errorlevel% equ 0 (
        echo ❌ ECHEC: Parsing manuel a aussi echoue
        echo Verifiez les logs de l'application pour les erreurs
    ) else (
        echo ✅ SUCCES: Parsing manuel a reussi !
        echo Le probleme etait avec le parsing asynchrone
    )
    
    del details_after_sync.html 2>nul
    del sync_result.html 2>nul
) else (
    echo ✅ SUCCES COMPLET: Text pairs trouves !
    echo Le parsing asynchrone fonctionne parfaitement
    
    echo.
    echo Verification du contenu des text pairs...
    findstr /i "Hello World\|Bonjour Monde" details_final.html >nul
    if %errorlevel% equ 0 (
        echo ✅ Contenu des text pairs correct
    ) else (
        echo ⚠️ Contenu des text pairs non trouve (peut-etre pagination)
    )
)

echo.
echo 10. Verification des erreurs...
findstr /i "error\|Error\|exception\|Exception" creation_result.html >nul
if %errorlevel% equ 0 (
    echo ⚠️ Erreurs detectees lors de la creation:
    findstr /i "error\|Error\|exception\|Exception" creation_result.html
) else (
    echo ✅ Aucune erreur detectee lors de la creation
)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del creation_result.html 2>nul
del datasets_immediate.html 2>nul
del details_final.html 2>nul

echo ========================================
echo RESULTATS DU TEST FINAL
echo ========================================
echo.
echo Si vous voyez "✅ SUCCES COMPLET", tout fonctionne parfaitement !
echo.
echo Verifications manuelles recommandees:
echo 1. Allez sur http://localhost:8081/admin/datasets
echo 2. Verifiez que "Test Final Dataset" est liste
echo 3. Cliquez sur "View Details" pour voir les text pairs
echo 4. Verifiez que 5 text pairs sont affiches
echo.
echo Si probleme, verifiez les logs de l'application pour:
echo - Messages d'erreur detailles
echo - Stack traces
echo - Messages de parsing asynchrone
echo.
pause
