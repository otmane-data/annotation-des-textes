@echo off
echo ========================================
echo TEST DU NOUVEAU TEMPLATE ANNOTATEUR
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

echo 5. Test du nouveau template...
set COOKIE_FILE=%TEMP%\test_cookies.txt

REM Connexion
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

REM Test d'acces aux details d'un annotateur
echo Test du template restructure...
curl -b "%COOKIE_FILE%" -s -w "Code: %%{http_code}\n" -o new_template.html http://localhost:8081/admin/annotateurs/details/1

echo.
echo 6. Verification du nouveau design...
if exist new_template.html (
    findstr /i "error\|exception\|500" new_template.html >nul
    if %errorlevel% equ 0 (
        echo ❌ ERREUR DETECTEE
        findstr /i "error\|exception\|500" new_template.html
    ) else (
        echo ✅ SUCCES - Template charge sans erreur
        echo.
        echo Verification des nouveaux elements...
        
        findstr /i "details-header" new_template.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Header moderne detecte
        ) else (
            echo ⚠️ Header moderne non trouve
        )
        
        findstr /i "content-grid" new_template.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Grille de contenu detectee
        ) else (
            echo ⚠️ Grille de contenu non trouvee
        )
        
        findstr /i "profile-card" new_template.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Carte de profil detectee
        ) else (
            echo ⚠️ Carte de profil non trouvee
        )
        
        findstr /i "stats-overview" new_template.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Vue d'ensemble des statistiques detectee
        ) else (
            echo ⚠️ Vue d'ensemble des statistiques non trouvee
        )
        
        findstr /i "tasks-section" new_template.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Section des taches detectee
        ) else (
            echo ⚠️ Section des taches non trouvee
        )
    )
    
    echo.
    echo Taille du fichier:
    for %%A in (new_template.html) do echo %%~zA octets
) else (
    echo ❌ Fichier de reponse non trouve
)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del new_template.html 2>nul

echo ========================================
echo FIN DU TEST
echo ========================================
pause
