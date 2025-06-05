@echo off
echo ========================================
echo TEST CORRECTION TEXT PAIRS
echo ========================================
echo.

echo 1. Creation d'un fichier CSV de test simple...
echo text1,text2 > test_pairs.csv
echo "Hello","World" >> test_pairs.csv
echo "Good","Bad" >> test_pairs.csv
echo "Yes","No" >> test_pairs.csv

echo ✅ Fichier test_pairs.csv cree avec 3 couples
echo.
echo Contenu du fichier:
type test_pairs.csv
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
set COOKIE_FILE=%TEMP%\test_pairs_cookies.txt

REM Connexion
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo Creation du dataset...
curl -b "%COOKIE_FILE%" ^
     -F "name=Test Text Pairs Fix" ^
     -F "description=Test pour corriger l'affichage des text pairs" ^
     -F "classes=positive;negative" ^
     -F "file=@test_pairs.csv" ^
     -s ^
     -o creation_result.html ^
     http://localhost:8081/admin/datasets/save

echo.
echo 4. Attente du parsing asynchrone (15 secondes)...
echo IMPORTANT: Surveillez la console pour:
echo - "Starting async parsing for dataset: Test Text Pairs Fix"
echo - "Using managed dataset: Test Text Pairs Fix (ID: X)"
echo - "DEBUG: Saving batch of X couples"
echo - "DEBUG: Successfully saved X couples"
echo - "Successfully parsed X rows from dataset file"

timeout /t 15 /nobreak >nul

echo.
echo 5. Verification des details du dataset...
curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets/details/1 > details_result.html

echo Recherche du nombre total de text pairs...
findstr /i "Total Text Pairs" details_result.html
echo.

echo Verification du contenu de la page...
findstr /i "No text pairs found" details_result.html >nul
if %errorlevel% equ 0 (
    echo ❌ PROBLEME: "No text pairs found" detecte
    echo.
    echo Verification des logs de debug dans la console...
    echo Les messages suivants devraient apparaitre:
    echo - "DEBUG: getCoupleTextsByDatasetId called with datasetId=1"
    echo - "DEBUG: Direct query found X couples for dataset 1"
    echo - "DEBUG: countCoupleTextsByDatasetId for dataset 1 = X"
    echo.
    echo Si ces messages n'apparaissent pas, le probleme est dans le parsing.
    echo Si ils apparaissent avec 0 couples, le probleme est dans la sauvegarde.
) else (
    echo ✅ SUCCES: Text pairs affiches correctement !
    
    echo Verification du contenu...
    findstr /i "Hello\|World\|Good\|Bad" details_result.html >nul
    if %errorlevel% equ 0 (
        echo ✅ Contenu des text pairs trouve
    ) else (
        echo ⚠️ Contenu specifique non trouve (peut-etre pagination)
    )
)

echo.
echo 6. Test direct de la base de donnees via API...
echo Verification du count direct...
curl -b "%COOKIE_FILE%" -s "http://localhost:8081/admin/datasets/details/1" | findstr /i "Total Text Pairs"

echo.
echo 7. Verification des erreurs...
findstr /i "error\|Error\|exception\|Exception" creation_result.html >nul
if %errorlevel% equ 0 (
    echo ⚠️ Erreurs detectees:
    findstr /i "error\|Error\|exception\|Exception" creation_result.html
) else (
    echo ✅ Aucune erreur detectee
)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del creation_result.html 2>nul
del details_result.html 2>nul

echo ========================================
echo DIAGNOSTIC COMPLET
echo ========================================
echo.
echo VERIFICATIONS MANUELLES:
echo 1. Allez sur http://localhost:8081/admin/datasets/details/1
echo 2. Verifiez le nombre dans "Total Text Pairs"
echo 3. Verifiez si les text pairs sont affiches en bas
echo.
echo LOGS A SURVEILLER:
echo 1. Messages de parsing: "Successfully parsed X rows"
echo 2. Messages de sauvegarde: "DEBUG: Successfully saved X couples"
echo 3. Messages de requete: "DEBUG: Direct query found X couples"
echo.
echo Si le parsing reussit mais l'affichage echoue:
echo - Probleme dans CoupleTextService.getCoupleTextsByDatasetId
echo - Probleme dans le template dataset_details.html
echo.
echo Si le parsing echoue:
echo - Probleme dans DatasetServiceImpl.ParseDataset
echo - Probleme de transaction ou de persistence
echo.
pause
