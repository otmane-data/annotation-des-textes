@echo off
echo ========================================
echo DEBUG PARSING DATASET
echo ========================================
echo.

echo 1. Creation d'un fichier CSV de test simple...
echo text1,text2 > debug_test.csv
echo Hello,World >> debug_test.csv
echo Good,Bad >> debug_test.csv
echo Yes,No >> debug_test.csv

echo ✅ Fichier debug_test.csv cree avec 3 lignes de donnees
echo.
echo Contenu du fichier:
type debug_test.csv
echo.

echo 2. Verification que l'application fonctionne...
curl -s -o nul http://localhost:8081/login
if %errorlevel% neq 0 (
    echo ❌ Application non accessible. Demarrez-la avec: mvnw.cmd spring-boot:run
    pause
    exit /b 1
)

echo ✅ Application accessible
echo.

echo 3. Test de creation de dataset...
set COOKIE_FILE=%TEMP%\debug_cookies.txt

REM Connexion
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo Creation du dataset...
curl -b "%COOKIE_FILE%" ^
     -F "name=Debug Dataset" ^
     -F "description=Dataset pour debug parsing" ^
     -F "classes=positive;negative" ^
     -F "file=@debug_test.csv" ^
     -s ^
     -o creation_result.html ^
     http://localhost:8081/admin/datasets/save

echo.
echo 4. Verification du resultat de creation...
findstr /i "error\|Error" creation_result.html >nul
if %errorlevel% equ 0 (
    echo ⚠️ Erreurs detectees lors de la creation:
    findstr /i "error\|Error" creation_result.html
) else (
    echo ✅ Creation sans erreur visible
)

echo.
echo 5. Attente du parsing asynchrone (5 secondes)...
timeout /t 5 /nobreak >nul

echo.
echo 6. Verification des details du dataset...
curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets/details/1 > details_result.html

echo Recherche du nombre de text pairs...
findstr /i "Total Text Pairs" details_result.html
echo.

findstr /i "No text pairs found" details_result.html >nul
if %errorlevel% equ 0 (
    echo ⚠️ Aucun text pair trouve - Le parsing a echoue
    echo.
    echo 7. Test du parsing synchrone...
    echo Declenchement du parsing manuel...
    curl -b "%COOKIE_FILE%" -s -o sync_result.html http://localhost:8081/admin/datasets/1/parse-sync
    
    echo Attente (3 secondes)...
    timeout /t 3 /nobreak >nul
    
    echo Verification apres parsing manuel...
    curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets/details/1 > details_after_sync.html
    
    findstr /i "Total Text Pairs" details_after_sync.html
    
    findstr /i "No text pairs found" details_after_sync.html >nul
    if %errorlevel% equ 0 (
        echo ❌ Parsing manuel a aussi echoue
        echo.
        echo Verification des erreurs dans la reponse:
        findstr /i "error\|Error\|exception\|Exception" sync_result.html
    ) else (
        echo ✅ Parsing manuel a reussi !
    )
    
    del details_after_sync.html 2>nul
    del sync_result.html 2>nul
) else (
    echo ✅ Text pairs trouves - Le parsing a reussi !
)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del creation_result.html 2>nul
del details_result.html 2>nul

echo ========================================
echo INSTRUCTIONS DE DEBUG
echo ========================================
echo.
echo 1. Si le parsing asynchrone echoue mais le manuel reussit:
echo    - Probleme avec @Async ou configuration Spring
echo.
echo 2. Si les deux echouent:
echo    - Verifiez les logs de l'application pour les erreurs
echo    - Verifiez que le fichier CSV est bien forme
echo.
echo 3. Logs a surveiller dans la console:
echo    - "Starting async parsing for dataset..."
echo    - "Parsing CSV file..."
echo    - "Successfully parsed X rows from dataset file"
echo    - Toute exception ou erreur
echo.
echo 4. Vous pouvez aussi tester manuellement:
echo    - Allez sur http://localhost:8081/admin/datasets/details/1
echo    - Cliquez sur "Re-parse Dataset (Debug)"
echo    - Surveillez les logs en temps reel
echo.
pause
