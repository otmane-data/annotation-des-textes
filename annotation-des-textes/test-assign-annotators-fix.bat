@echo off
echo ========================================
echo TEST CORRECTION ASSIGN ANNOTATORS
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
set COOKIE_FILE=%TEMP%\test_assign_fix_cookies.txt
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo.
echo 3. Test de la page d'assignation...
echo Acces a la page d'assignation pour dataset 1...
curl -b "%COOKIE_FILE%" -s -o assign_page.html http://localhost:8081/admin/datasets/1/assign_annotator

echo Verification des erreurs...
findstr /i "error\|Error\|exception\|Exception\|500\|404" assign_page.html >nul
if %errorlevel% equ 0 (
    echo ❌ ERREURS DETECTEES sur la page d'assignation:
    findstr /i "error\|Error\|exception\|Exception\|500\|404" assign_page.html
    echo.
    echo Premiers 500 caracteres de la reponse:
    powershell -command "Get-Content assign_page.html -Raw | ForEach-Object { $_.Substring(0, [Math]::Min(500, $_.Length)) }"
) else (
    echo ✅ Page d'assignation accessible sans erreur
    
    echo.
    echo Verification du contenu de la page...
    findstr /i "Select Annotators\|deadline\|selectedAnnotateurs" assign_page.html >nul
    if %errorlevel% equ 0 (
        echo ✅ Elements du formulaire detectes
        
        echo.
        echo Verification des annotateurs disponibles...
        findstr /i "annotateur-checkbox\|checkbox-wrapper" assign_page.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Annotateurs detectes dans la page
        ) else (
            echo ⚠️ Aucun annotateur detecte (peut-etre aucun annotateur en base)
        )
        
    ) else (
        echo ❌ Elements du formulaire non detectes
    )
)

echo.
echo 4. Test d'assignation avec donnees invalides...
echo Test 1: Aucun annotateur selectionne...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=2024-12-31" ^
     -X POST ^
     -s ^
     -o assign_result_empty.html ^
     http://localhost:8081/admin/datasets/1/assign

echo Verification du message d'erreur...
findstr /i "Please select at least one annotator" assign_result_empty.html >nul
if %errorlevel% equ 0 (
    echo ✅ Gestion d'erreur correcte pour aucun annotateur
) else (
    echo ⚠️ Message d'erreur pour aucun annotateur non detecte
    echo Verification d'autres messages d'erreur...
    findstr /i "error\|Error" assign_result_empty.html
)

echo.
echo Test 2: Moins de 3 annotateurs...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=2024-12-31" ^
     -d "selectedAnnotateurs=1" ^
     -d "selectedAnnotateurs=2" ^
     -X POST ^
     -s ^
     -o assign_result_few.html ^
     http://localhost:8081/admin/datasets/1/assign

echo Verification du message d'erreur pour moins de 3 annotateurs...
findstr /i "Au moins 3 annotateurs" assign_result_few.html >nul
if %errorlevel% equ 0 (
    echo ✅ Gestion d'erreur correcte pour moins de 3 annotateurs
) else (
    echo ⚠️ Message d'erreur pour moins de 3 annotateurs non detecte
    echo Verification d'autres messages d'erreur...
    findstr /i "error\|Error" assign_result_few.html
)

echo.
echo Test 3: Date invalide...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=invalid-date" ^
     -d "selectedAnnotateurs=1" ^
     -d "selectedAnnotateurs=2" ^
     -d "selectedAnnotateurs=3" ^
     -X POST ^
     -s ^
     -o assign_result_date.html ^
     http://localhost:8081/admin/datasets/1/assign

echo Verification de la gestion de date invalide...
findstr /i "error\|Error" assign_result_date.html >nul
if %errorlevel% equ 0 (
    echo ✅ Gestion d'erreur pour date invalide detectee
) else (
    echo ⚠️ Gestion d'erreur pour date invalide non detectee
)

echo.
echo 5. Test d'assignation valide (si 3+ annotateurs existent)...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=2024-12-31" ^
     -d "selectedAnnotateurs=1" ^
     -d "selectedAnnotateurs=2" ^
     -d "selectedAnnotateurs=3" ^
     -X POST ^
     -s ^
     -o assign_result_success.html ^
     http://localhost:8081/admin/datasets/1/assign

echo Verification du resultat...
findstr /i "successfully assigned\|success" assign_result_success.html >nul
if %errorlevel% equ 0 (
    echo ✅ Assignation reussie detectee
) else (
    echo ⚠️ Assignation reussie non detectee
    
    echo Verification des erreurs...
    findstr /i "error\|Error" assign_result_success.html >nul
    if %errorlevel% equ 0 (
        echo ❌ Erreurs detectees lors de l'assignation:
        findstr /i "error\|Error" assign_result_success.html
    ) else (
        echo ⚠️ Aucun message de succes ou d'erreur detecte
    )
)

echo.
echo 6. Verification des logs de debug...
echo IMPORTANT: Verifiez dans la console de l'application:
echo - "DEBUG: Processing assignment for dataset 1"
echo - "DEBUG: Selected annotators: [1, 2, 3]"
echo - "DEBUG: Found X annotators in database"
echo - "DEBUG: Assignment completed successfully"
echo.
echo Si vous voyez des erreurs comme:
echo - "DEBUG: No annotators selected"
echo - "DEBUG: Not enough annotators selected"
echo - "DEBUG: Some annotators not found in database"
echo - "DEBUG: Error during assignment: ..."
echo Cela indique le probleme specifique.

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del assign_page.html 2>nul
del assign_result_empty.html 2>nul
del assign_result_few.html 2>nul
del assign_result_date.html 2>nul
del assign_result_success.html 2>nul

echo ========================================
echo RESULTATS DU TEST
echo ========================================
echo.
echo VERIFICATION MANUELLE RECOMMANDEE:
echo 1. Allez sur http://localhost:8081/admin/datasets/details/1
echo 2. Cliquez sur "Assign Annotators"
echo 3. Verifiez que la page se charge sans erreur
echo 4. Selectionnez au moins 3 annotateurs
echo 5. Definissez une deadline
echo 6. Cliquez sur "Assign Selected Annotators"
echo 7. Verifiez le message de succes ou d'erreur
echo.
echo PROBLEMES CORRIGES:
echo ✅ Logique de validation corrigee (null check avant size check)
echo ✅ Template corrige (deadlineDate au lieu de deadline)
echo ✅ Gestion d'erreur amelioree avec logs de debug
echo ✅ Messages d'erreur plus clairs
echo ✅ Redirection correcte en cas d'erreur
echo.
echo CAUSES POSSIBLES D'ERREUR RESTANTES:
echo 1. Aucun annotateur en base de donnees
echo 2. Probleme dans AssignTaskToAnnotator.assignTaskToAnnotator()
echo 3. Probleme de format de date
echo 4. Probleme de transaction ou de persistence
echo.
echo Si tous les tests montrent "✅", le bouton assign annotateurs fonctionne !
echo.
pause
