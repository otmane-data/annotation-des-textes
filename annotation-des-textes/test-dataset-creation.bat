@echo off
echo ========================================
echo TEST DE CREATION DE DATASET
echo ========================================
echo.

echo 1. Arret des processus existants...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do (
    taskkill /F /PID %%a 2>nul
)

echo.
echo 2. Compilation...
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

echo 5. Test d'acces a la page de creation...
set COOKIE_FILE=%TEMP%\test_cookies.txt

REM Connexion
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

REM Test d'acces a la page d'ajout de dataset
echo Test d'acces a la page d'ajout...
curl -b "%COOKIE_FILE%" -s -w "Code: %%{http_code}\n" -o add_page.html http://localhost:8081/admin/datasets/add

echo.
echo 6. Verification de la page d'ajout...
if exist add_page.html (
    findstr /i "error\|exception\|500" add_page.html >nul
    if %errorlevel% equ 0 (
        echo ❌ ERREUR sur la page d'ajout
    ) else (
        echo ✅ Page d'ajout accessible
        
        echo.
        echo Verification du formulaire...
        findstr /i "form.*datasets/save" add_page.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Formulaire de creation trouve
        ) else (
            echo ⚠️ Formulaire de creation non trouve
        )
    )
) else (
    echo ❌ Page d'ajout non accessible
)

echo.
echo 7. Creation d'un fichier de test...
echo text1,text2 > test_dataset.csv
echo "Hello","World" >> test_dataset.csv
echo "Bonjour","Monde" >> test_dataset.csv

echo ✅ Fichier de test cree: test_dataset.csv

echo.
echo 8. IMPORTANT: Pour tester la creation de dataset:
echo    1. Ouvrez votre navigateur sur http://localhost:8081
echo    2. Connectez-vous avec admin/admin
echo    3. Allez dans Dataset Management
echo    4. Cliquez sur "Add Dataset"
echo    5. Remplissez le formulaire avec:
echo       - Name: Test Dataset
echo       - Description: Test description
echo       - File: test_dataset.csv (cree ci-dessus)
echo       - Classes: positive;negative
echo    6. Cliquez sur "Create Dataset"
echo.
echo    Verifiez les logs dans la console pour voir les messages de debug
echo    qui commencent par "Creating dataset:" et "Error creating dataset:"

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del add_page.html 2>nul

echo ========================================
echo FIN DU TEST
echo ========================================
echo.
echo Le fichier test_dataset.csv a ete cree pour vos tests.
echo Verifiez les logs dans la console de l'application pour diagnostiquer les erreurs.
echo.
pause
