@echo off
echo ========================================
echo DIAGNOSTIC CREATION DATASET
echo ========================================
echo.

echo 1. Verification de l'application...
curl -s -o nul http://localhost:8081/login
if %errorlevel% neq 0 (
    echo ❌ Application non accessible sur localhost:8081
    echo Demarrez l'application avec: mvnw.cmd spring-boot:run
    pause
    exit /b 1
)

echo ✅ Application accessible
echo.

echo 2. Test de connexion...
set COOKIE_FILE=%TEMP%\diagnostic_cookies.txt

curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s -w "Code: %%{http_code}\n" http://localhost:8081/login > login_result.txt

findstr "200" login_result.txt >nul
if %errorlevel% neq 0 (
    echo ❌ Probleme de connexion
    type login_result.txt
    pause
    exit /b 1
)

echo ✅ Connexion reussie
echo.

echo 3. Test d'acces a la page d'ajout...
curl -b "%COOKIE_FILE%" -s -w "Code: %%{http_code}\n" -o add_page.html http://localhost:8081/admin/datasets/add

findstr /i "error\|exception\|500" add_page.html >nul
if %errorlevel% equ 0 (
    echo ❌ Erreur sur la page d'ajout
    findstr /i "error\|exception\|500" add_page.html
    pause
    exit /b 1
)

echo ✅ Page d'ajout accessible
echo.

echo 4. Verification du formulaire...
findstr /i "action.*datasets/save" add_page.html >nul
if %errorlevel% equ 0 (
    echo ✅ Action du formulaire trouvee
) else (
    echo ❌ Action du formulaire non trouvee
    echo Recherche de formulaires...
    findstr /i "<form" add_page.html
)

findstr /i "enctype.*multipart" add_page.html >nul
if %errorlevel% equ 0 (
    echo ✅ Enctype multipart trouve
) else (
    echo ❌ Enctype multipart non trouve
)

findstr /i "name.*file" add_page.html >nul
if %errorlevel% equ 0 (
    echo ✅ Input file trouve
) else (
    echo ❌ Input file non trouve
)

findstr /i "name.*classes" add_page.html >nul
if %errorlevel% equ 0 (
    echo ✅ Input classes trouve
) else (
    echo ❌ Input classes non trouve
)

echo.
echo 5. Creation d'un fichier de test...
echo text1,text2 > test_simple.csv
echo Hello,World >> test_simple.csv
echo Bonjour,Monde >> test_simple.csv

echo ✅ Fichier test_simple.csv cree
echo.

echo 6. Test de soumission du formulaire...
echo Tentative de soumission avec curl...

curl -b "%COOKIE_FILE%" ^
     -F "name=Test Dataset Diagnostic" ^
     -F "description=Test de diagnostic" ^
     -F "classes=positive;negative" ^
     -F "file=@test_simple.csv" ^
     -w "Code: %%{http_code}\n" ^
     -o submit_result.html ^
     http://localhost:8081/admin/datasets/save

echo.
echo Resultat de la soumission:
type submit_result.html | findstr /i "error\|exception\|success\|redirect"

echo.
echo 7. Verification des logs...
echo IMPORTANT: Verifiez la console de l'application pour:
echo - Messages commencant par "Creating dataset:"
echo - Messages d'erreur avec stack traces
echo - Messages de validation

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del login_result.txt 2>nul
del add_page.html 2>nul
del submit_result.html 2>nul

echo ========================================
echo FIN DU DIAGNOSTIC
echo ========================================
echo.
echo Si tout est ✅ mais le bouton ne fonctionne pas:
echo 1. Verifiez la console du navigateur (F12) pour erreurs JavaScript
echo 2. Verifiez que tous les champs sont remplis
echo 3. Verifiez la console de l'application pour logs detailles
echo.
pause
