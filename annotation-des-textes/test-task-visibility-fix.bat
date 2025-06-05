@echo off
echo ========================================
echo TEST CORRECTION VISIBILITE DES TACHES
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
set COOKIE_FILE=%TEMP%\test_visibility_cookies.txt
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo.
echo 3. Test d'assignation avec deadline future...
echo Assignation avec deadline dans le futur (2025-12-31)...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=2025-12-31" ^
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
echo 5. Test de connexion annotateur...
echo Connexion en tant qu'annotateur 1...
set COOKIE_ANN=%TEMP%\test_annotator_cookies.txt

echo Tentative de connexion avec annotateur1/password...
curl -c "%COOKIE_ANN%" -d "username=annotateur1&password=password" -X POST -L -s http://localhost:8081/login

echo Test de la page des taches annotateur...
curl -b "%COOKIE_ANN%" -s -o tasks_annotator.html http://localhost:8081/user/tasks

findstr /i "No tasks assigned yet\|My Tasks" tasks_annotator.html >nul
if %errorlevel% equ 0 (
    findstr /i "No tasks assigned yet" tasks_annotator.html >nul
    if %errorlevel% equ 0 (
        echo ❌ Annotateur: Aucune tache visible
    ) else (
        echo ✅ Annotateur: Taches detectees
    )
) else (
    echo ⚠️ Annotateur: Page inattendue
)

echo.
echo 6. Test avec differents utilisateurs...
echo Test avec admin/admin...
curl -c "%COOKIE_ANN%_admin" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login
curl -b "%COOKIE_ANN%_admin" -s -o tasks_admin.html http://localhost:8081/user/tasks

echo Test avec annotateur2/password...
curl -c "%COOKIE_ANN%_ann2" -d "username=annotateur2&password=password" -X POST -L -s http://localhost:8081/login
curl -b "%COOKIE_ANN%_ann2" -s -o tasks_ann2.html http://localhost:8081/user/tasks

echo.
echo 7. Verification des logs de l'application...
echo IMPORTANT: Verifiez dans la console de l'application:
echo - "DEBUG: Getting valid tasks for annotator ID: X"
echo - "DEBUG: Total tasks in database: X"
echo - "DEBUG: Tasks for user X: X"
echo - "DEBUG: Valid (non-expired) tasks: X"
echo - "DEBUG: Task ID X, deadline: ..."

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del "%COOKIE_ANN%" 2>nul
del "%COOKIE_ANN%_admin" 2>nul
del "%COOKIE_ANN%_ann2" 2>nul
del assign_result.html 2>nul
del debug_tasks.txt 2>nul
del tasks_annotator.html 2>nul
del tasks_admin.html 2>nul
del tasks_ann2.html 2>nul

echo ========================================
echo DIAGNOSTIC COMPLET
echo ========================================
echo.
echo PROBLEME IDENTIFIE ET CORRIGE:
echo ✅ Filtrage de date trop strict dans getValidTasksForAnnotateur()
echo ✅ Taches avec deadline aujourd'hui etaient considerees comme expirees
echo ✅ Ajout de logs de debug detailles
echo ✅ Inclusion des taches avec deadline = aujourd'hui
echo.
echo CORRECTION APPORTEE:
echo - Changement de "dateLimite.after(currentDate)" 
echo - Vers "dateLimite.after(currentDate) OR isSameDay(dateLimite, currentDate)"
echo - Ajout de logs de debug pour diagnostiquer
echo.
echo VERIFICATION MANUELLE:
echo 1. Redemarrez l'application
echo 2. Assignez des taches avec deadline future (ex: 2025-12-31)
echo 3. Connectez-vous en tant qu'annotateur
echo 4. Allez sur http://localhost:8081/user/tasks
echo 5. Verifiez que les taches apparaissent
echo.
echo LOGS DE DEBUG A SURVEILLER:
echo - "DEBUG: Getting valid tasks for annotator ID: X"
echo - "DEBUG: Total tasks in database: X" (doit etre > 0)
echo - "DEBUG: Tasks for user X: X" (doit etre > 0 si taches assignees)
echo - "DEBUG: Valid (non-expired) tasks: X" (doit etre > 0)
echo.
echo Si "Total tasks in database: 0" → Probleme d'assignation
echo Si "Tasks for user X: 0" → Probleme d'ID utilisateur ou assignation
echo Si "Valid tasks: 0" → Probleme de deadline (taches expirees)
echo.
pause
