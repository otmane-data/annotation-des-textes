@echo off
echo ========================================
echo TEST DE LA FONCTIONNALITE DETAILS ANNOTATEUR
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

echo Application demarree !
echo.

echo 5. Test d'acces aux annotateurs...
set COOKIE_FILE=%TEMP%\test_cookies.txt

REM Connexion
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

REM Test d'acces a la liste des annotateurs
echo Test de la liste des annotateurs...
curl -b "%COOKIE_FILE%" -s -w "Code: %%{http_code}\n" http://localhost:8081/admin/annotateurs

echo.
echo Test des details d'un annotateur (ID 1)...
curl -b "%COOKIE_FILE%" -s -w "Code: %%{http_code}\n" -o annotator_details.html http://localhost:8081/admin/annotateurs/details/1

echo.
echo 6. Verification du resultat...
if exist annotator_details.html (
    findstr /i "error\|exception\|500" annotator_details.html >nul
    if %errorlevel% equ 0 (
        echo ❌ ERREUR DETECTEE
        findstr /i "error\|exception\|500" annotator_details.html
    ) else (
        echo ✅ SUCCES - Page chargee sans erreur
        echo Verification du contenu...
        findstr /i "Annotator Details\|Profile Information" annotator_details.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Contenu correct detecte
        ) else (
            echo ⚠️ Contenu inattendu
        )
    )
    
    echo.
    echo Taille du fichier:
    for %%A in (annotator_details.html) do echo %%~zA octets
) else (
    echo ❌ Fichier de reponse non trouve
)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del annotator_details.html 2>nul

echo ========================================
echo FIN DU TEST
echo ========================================
pause
