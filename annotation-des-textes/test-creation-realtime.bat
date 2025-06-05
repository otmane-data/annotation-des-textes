@echo off
echo ========================================
echo TEST CREATION DATASET EN TEMPS REEL
echo ========================================
echo.

echo 1. Creation d'un fichier de test...
echo text1,text2 > test_dataset.csv
echo "Hello","World" >> test_dataset.csv
echo "Bonjour","Monde" >> test_dataset.csv
echo "Good","Bad" >> test_dataset.csv

echo ✅ Fichier test_dataset.csv cree
echo.

echo 2. Test de creation via curl...
set COOKIE_FILE=%TEMP%\test_cookies.txt

REM Connexion
echo Connexion...
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo.
echo 3. Soumission du formulaire avec logs detailles...
echo IMPORTANT: Surveillez la console de l'application pendant cette operation

curl -b "%COOKIE_FILE%" ^
     -F "name=Test Dataset Debug" ^
     -F "description=Dataset de test pour debug" ^
     -F "classes=positive;negative;neutral" ^
     -F "file=@test_dataset.csv" ^
     -v ^
     -L ^
     -o creation_result.html ^
     http://localhost:8081/admin/datasets/save

echo.
echo 4. Analyse du resultat...
echo Taille du fichier de reponse:
for %%A in (creation_result.html) do echo %%~zA octets

echo.
echo Contenu de la reponse (premiers 500 caracteres):
powershell -command "Get-Content creation_result.html -Raw | ForEach-Object { $_.Substring(0, [Math]::Min(500, $_.Length)) }"

echo.
echo 5. Verification si le dataset a ete cree...
curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets > datasets_list.html

findstr /i "Test Dataset Debug" datasets_list.html >nul
if %errorlevel% equ 0 (
    echo ✅ Dataset trouve dans la liste
) else (
    echo ❌ Dataset non trouve dans la liste
)

echo.
echo 6. Recherche d'erreurs dans la reponse...
findstr /i "error\|exception\|failed\|invalid" creation_result.html >nul
if %errorlevel% equ 0 (
    echo ⚠️ Erreurs detectees:
    findstr /i "error\|exception\|failed\|invalid" creation_result.html
) else (
    echo ✅ Aucune erreur visible dans la reponse
)

echo.
echo 7. Verification des messages de succes/erreur...
findstr /i "success\|successfully\|created" creation_result.html >nul
if %errorlevel% equ 0 (
    echo ✅ Messages de succes detectes:
    findstr /i "success\|successfully\|created" creation_result.html
) else (
    echo ⚠️ Aucun message de succes detecte
)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del creation_result.html 2>nul
del datasets_list.html 2>nul

echo ========================================
echo RESULTATS DU TEST
echo ========================================
echo.
echo IMPORTANT: Pendant ce test, verifiez dans la console de l'application:
echo 1. Messages "Creating dataset: name='Test Dataset Debug'..."
echo 2. Messages "Processing file upload..."
echo 3. Messages "Dataset saved successfully with ID: X"
echo 4. Toute exception ou erreur
echo.
echo Si aucun log n'apparait, le probleme est dans le mapping du controleur.
echo Si des logs apparaissent mais avec erreurs, le probleme est dans la logique.
echo.
pause
