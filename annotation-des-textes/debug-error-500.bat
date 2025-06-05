@echo off
echo ========================================
echo DEBUG ERREUR 500 - ASSIGN ANNOTATORS
echo ========================================
echo.

echo 1. Verification de l'application...
curl -s -o nul http://localhost:8081/login
if %errorlevel% neq 0 (
    echo ❌ Application non accessible. Demarrez-la avec: mvnw.cmd spring-boot:run
    pause
    exit /b 1
)

echo ✅ Application accessible
echo.

echo 2. Connexion admin...
set COOKIE_FILE=%TEMP%\debug_500_cookies.txt
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo.
echo 3. Verification des donnees de base...
echo Test de l'endpoint de debug pour dataset 1...
curl -b "%COOKIE_FILE%" -s http://localhost:8081/admin/datasets/1/debug

echo.
echo.
echo 4. Test de la page d'assignation...
echo Acces a la page d'assignation...
curl -b "%COOKIE_FILE%" -s -o assign_page_debug.html http://localhost:8081/admin/datasets/1/assign_annotator

findstr /i "error\|Error\|500" assign_page_debug.html >nul
if %errorlevel% equ 0 (
    echo ❌ Erreur sur la page d'assignation
    findstr /i "error\|Error\|500" assign_page_debug.html
) else (
    echo ✅ Page d'assignation accessible
    
    echo Verification des annotateurs disponibles...
    findstr /i "annotateur-checkbox" assign_page_debug.html >nul
    if %errorlevel% equ 0 (
        echo ✅ Annotateurs detectes
    ) else (
        echo ⚠️ Aucun annotateur detecte
    )
)

echo.
echo 5. Test d'assignation avec logs detailles...
echo IMPORTANT: Surveillez la console de l'application pour:
echo - "DEBUG: Processing assignment for dataset 1"
echo - "DEBUG: Starting task assignment for dataset: [nom]"
echo - "DEBUG: Number of annotators: X"
echo - "DEBUG: Found X text pairs in dataset"
echo - "DEBUG: Creating tasks in database..."
echo - "DEBUG: Successfully created X tasks"
echo.
echo OU des erreurs comme:
echo - "ERROR: Failed to assign tasks - [message]"
echo - Stack traces d'exceptions
echo.

echo Test avec 3 annotateurs...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=2024-12-31" ^
     -d "selectedAnnotateurs=1" ^
     -d "selectedAnnotateurs=2" ^
     -d "selectedAnnotateurs=3" ^
     -X POST ^
     -s ^
     -w "HTTP Status: %%{http_code}\n" ^
     -o assign_result_debug.html ^
     http://localhost:8081/admin/datasets/1/assign

echo.
echo Verification du code de reponse HTTP...
echo Si vous voyez "HTTP Status: 500", c'est l'erreur serveur.
echo Si vous voyez "HTTP Status: 302", c'est une redirection (probablement succes).

echo.
echo Verification du contenu de la reponse...
findstr /i "500\|Unknown error\|Internal Server Error" assign_result_debug.html >nul
if %errorlevel% equ 0 (
    echo ❌ ERREUR 500 CONFIRMEE
    echo.
    echo Contenu de la page d'erreur:
    type assign_result_debug.html
) else (
    findstr /i "success\|successfully" assign_result_debug.html >nul
    if %errorlevel% equ 0 (
        echo ✅ SUCCES DETECTE
    ) else (
        findstr /i "error\|Error" assign_result_debug.html >nul
        if %errorlevel% equ 0 (
            echo ⚠️ Erreur detectee (pas 500):
            findstr /i "error\|Error" assign_result_debug.html
        ) else (
            echo ⚠️ Reponse inattendue
            echo Premiers 200 caracteres:
            powershell -command "Get-Content assign_result_debug.html -Raw | ForEach-Object { $_.Substring(0, [Math]::Min(200, $_.Length)) }"
        )
    )
)

echo.
echo 6. Test avec moins d'annotateurs pour verifier la validation...
echo Test avec 1 annotateur (devrait echouer avec message d'erreur)...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=2024-12-31" ^
     -d "selectedAnnotateurs=1" ^
     -X POST ^
     -s ^
     -w "HTTP Status: %%{http_code}\n" ^
     -o assign_result_1_annotator.html ^
     http://localhost:8081/admin/datasets/1/assign

findstr /i "Au moins 3 annotateurs" assign_result_1_annotator.html >nul
if %errorlevel% equ 0 (
    echo ✅ Validation correcte pour moins de 3 annotateurs
) else (
    echo ⚠️ Validation pour moins de 3 annotateurs non detectee
)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del assign_page_debug.html 2>nul
del assign_result_debug.html 2>nul
del assign_result_1_annotator.html 2>nul

echo ========================================
echo DIAGNOSTIC ERREUR 500
echo ========================================
echo.
echo CAUSES POSSIBLES DE L'ERREUR 500:
echo.
echo 1. LAZYINITIALIZATIONEXCEPTION (CORRIGEE):
echo    - Acces a annotator.getTaches() hors session Hibernate
echo    - Solution: Utilisation du repository dans hasAnnotatorAlreadyReceivedOriginalPair()
echo.
echo 2. NULLPOINTEREXCEPTION:
echo    - Dataset null ou annotateurs null
echo    - Solution: Validation ajoutee au debut de assignTaskToAnnotator()
echo.
echo 3. PROBLEME DE CONSTRUCTEUR:
echo    - new CoupleText(couple) echoue
echo    - Solution: Constructeur de copie existe et semble correct
echo.
echo 4. PROBLEME DE BASE DE DONNEES:
echo    - Aucun text pair dans le dataset
echo    - Annotateurs inexistants
echo    - Contraintes de base de donnees violees
echo.
echo 5. PROBLEME DE TRANSACTION:
echo    - Conflit de transaction lors de la sauvegarde
echo    - Solution: Gestion d'erreur ajoutee avec try-catch
echo.
echo VERIFICATION MANUELLE RECOMMANDEE:
echo 1. Redemarrez l'application
echo 2. Verifiez qu'il y a des text pairs dans le dataset 1
echo 3. Verifiez qu'il y a au moins 3 annotateurs actifs
echo 4. Surveillez les logs de la console lors de l'assignation
echo 5. Testez avec un dataset different si necessaire
echo.
echo Si l'erreur 500 persiste, verifiez les logs de l'application
echo pour la stack trace complete de l'exception.
echo.
pause
