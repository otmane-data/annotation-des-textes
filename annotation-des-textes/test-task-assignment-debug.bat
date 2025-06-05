@echo off
echo ========================================
echo DEBUG ASSIGNATION DES TACHES
echo ========================================
echo.

echo 1. Verification de l'application...
curl -s -o nul http://localhost:8081/login
if %errorlevel% neq 0 (
    echo ❌ Application non accessible. Redemarrez avec: mvnw.cmd spring-boot:run
    pause
    exit /b 1
)

echo ✅ Application accessible
echo.

echo 2. Connexion admin...
set COOKIE_FILE=%TEMP%\test_debug_cookies.txt
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo.
echo 3. Test d'assignation...
echo Assignation de 3 annotateurs au dataset 23...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=2025-06-03" ^
     -d "selectedAnnotateurs=1" ^
     -d "selectedAnnotateurs=2" ^
     -d "selectedAnnotateurs=3" ^
     -X POST ^
     -s ^
     -w "HTTP Status: %%{http_code}\n" ^
     -o assign_result.html ^
     http://localhost:8081/admin/datasets/23/assign

echo.
echo Verification du resultat de l'assignation...
findstr /i "success\|successfully" assign_result.html >nul
if %errorlevel% equ 0 (
    echo ✅ Assignation semble reussie
) else (
    findstr /i "error\|Error" assign_result.html >nul
    if %errorlevel% equ 0 (
        echo ❌ Erreur lors de l'assignation:
        findstr /i "error\|Error" assign_result.html
    ) else (
        echo ⚠️ Resultat inattendu
    )
)

echo.
echo 4. Debug des taches creees...
echo Verification des taches en base de donnees...
curl -b "%COOKIE_FILE%" -s -o debug_tasks.txt http://localhost:8081/admin/datasets/23/debug-tasks

echo.
echo === CONTENU DU DEBUG ===
type debug_tasks.txt
echo === FIN DU DEBUG ===

echo.
echo 5. Verification des taches pour chaque annotateur...
echo.
echo Verification des taches pour annotateur ID=1...
curl -b "%COOKIE_FILE%" -s -o tasks_annotator_1.html http://localhost:8081/annotateur/tasks

echo Connexion en tant qu'annotateur 1...
curl -c "%COOKIE_FILE%_ann1" -d "username=annotateur1&password=password" -X POST -L -s http://localhost:8081/login

curl -b "%COOKIE_FILE%_ann1" -s -o tasks_annotator_1.html http://localhost:8081/annotateur/tasks

findstr /i "No Tasks Assigned\|Assigned Tasks" tasks_annotator_1.html >nul
if %errorlevel% equ 0 (
    findstr /i "No Tasks Assigned" tasks_annotator_1.html >nul
    if %errorlevel% equ 0 (
        echo ❌ Annotateur 1: Aucune tache assignee
    ) else (
        echo ✅ Annotateur 1: Taches detectees
    )
) else (
    echo ⚠️ Annotateur 1: Page inattendue
)

echo.
echo 6. Verification des logs de l'application...
echo IMPORTANT: Verifiez dans la console de l'application:
echo - "DEBUG: Assignment completed successfully"
echo - Messages du service AssignTaskToAnnotator
echo - "DEBUG: Created task for annotator: ..."
echo - "DEBUG: Successfully created X tasks"

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del "%COOKIE_FILE%_ann1" 2>nul
del assign_result.html 2>nul
del debug_tasks.txt 2>nul
del tasks_annotator_1.html 2>nul

echo ========================================
echo DIAGNOSTIC
echo ========================================
echo.
echo CAUSES POSSIBLES DU PROBLEME:
echo.
echo 1. TACHES NON CREEES EN BASE:
echo    - Erreur dans AssignTaskToAnnotator.assignTaskToAnnotator()
echo    - Transaction non commitee
echo    - Exception silencieuse
echo.
echo 2. TACHES CREEES MAIS NON VISIBLES:
echo    - Probleme dans getValidTasksForAnnotateur()
echo    - Date limite incorrecte (taches expirees)
echo    - Annotateur ID incorrect
echo.
echo 3. PROBLEME D'INTERFACE:
echo    - Template annotateur_tasks.html incorrect
echo    - Controleur annotateur incorrect
echo    - Session/authentification incorrecte
echo.
echo 4. PROBLEME DE DONNEES:
echo    - Dataset sans couples de texte
echo    - Annotateurs inexistants
echo    - Contraintes de base de donnees
echo.
echo VERIFICATION MANUELLE:
echo 1. Consultez le debug des taches ci-dessus
echo 2. Verifiez les logs de l'application
echo 3. Connectez-vous en tant qu'annotateur
echo 4. Allez sur la page des taches assignees
echo.
echo Si le debug montre "Total tasks found: 0",
echo le probleme est dans l'assignation.
echo Si il montre des taches mais l'annotateur ne les voit pas,
echo le probleme est dans l'affichage.
echo.
pause
