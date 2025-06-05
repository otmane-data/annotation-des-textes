@echo off
echo ========================================
echo TEST DES BOUTONS DE TACHES
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

echo 5. Test des pages de taches...
set COOKIE_FILE=%TEMP%\test_cookies.txt

REM Connexion
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo Test de la page des taches...
curl -b "%COOKIE_FILE%" -s -w "Code: %%{http_code}\n" -o tasks_page.html http://localhost:8081/admin/tasks

echo.
echo Test de la page des datasets...
curl -b "%COOKIE_FILE%" -s -w "Code: %%{http_code}\n" http://localhost:8081/admin/datasets

echo.
echo Test de la page d'ajout de dataset...
curl -b "%COOKIE_FILE%" -s -w "Code: %%{http_code}\n" http://localhost:8081/admin/datasets/add

echo.
echo 6. Verification du contenu de la page des taches...
if exist tasks_page.html (
    findstr /i "error\|exception\|500" tasks_page.html >nul
    if %errorlevel% equ 0 (
        echo ❌ ERREUR DETECTEE dans la page des taches
        findstr /i "error\|exception\|500" tasks_page.html
    ) else (
        echo ✅ Page des taches chargee sans erreur
        
        echo Verification des nouveaux boutons...
        findstr /i "Manage Datasets" tasks_page.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Bouton "Manage Datasets" detecte
        ) else (
            echo ⚠️ Bouton "Manage Datasets" non trouve
        )
        
        findstr /i "Add Dataset" tasks_page.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Bouton "Add Dataset" detecte
        ) else (
            echo ⚠️ Bouton "Add Dataset" non trouve
        )
        
        findstr /i "header-actions" tasks_page.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Section header-actions detectee
        ) else (
            echo ⚠️ Section header-actions non trouvee
        )
        
        findstr /i "automatically generated" tasks_page.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Message explicatif detecte
        ) else (
            echo ⚠️ Message explicatif non trouve
        )
    )
    
    echo.
    echo Taille du fichier:
    for %%A in (tasks_page.html) do echo %%~zA octets
) else (
    echo ❌ Fichier de reponse non trouve
)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del tasks_page.html 2>nul

echo ========================================
echo FIN DU TEST
echo ========================================
pause
