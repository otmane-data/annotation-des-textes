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
set COOKIE_FILE=%TEMP%\test_assignment_debug_cookies.txt
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo.
echo 3. Debug complet de l'assignation...
echo Test automatique de l'assignation pour dataset 23...
curl -b "%COOKIE_FILE%" -s -o assignment_debug.txt http://localhost:8081/admin/datasets/23/debug-assignment

echo.
echo === RESULTAT DU DEBUG ASSIGNATION ===
type assignment_debug.txt
echo === FIN DU DEBUG ===

echo.
echo 4. Verification des taches apres debug...
curl -b "%COOKIE_FILE%" -s -o tasks_after_debug.txt http://localhost:8081/admin/datasets/23/debug-tasks

echo.
echo === TACHES APRES DEBUG ===
type tasks_after_debug.txt
echo === FIN DES TACHES ===

echo.
echo 5. Test manuel d'assignation...
echo Assignation manuelle avec deadline future...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=2025-12-31" ^
     -d "selectedAnnotateurs=1" ^
     -d "selectedAnnotateurs=2" ^
     -d "selectedAnnotateurs=3" ^
     -X POST ^
     -s ^
     -w "HTTP Status: %%{http_code}\n" ^
     -o manual_assign_result.html ^
     http://localhost:8081/admin/datasets/23/assign

echo.
echo Verification du resultat de l'assignation manuelle...
findstr /i "success\|successfully" manual_assign_result.html >nul
if %errorlevel% equ 0 (
    echo ✅ Assignation manuelle semble reussie
) else (
    findstr /i "error\|Error" manual_assign_result.html >nul
    if %errorlevel% equ 0 (
        echo ❌ Erreur lors de l'assignation manuelle:
        findstr /i "error\|Error" manual_assign_result.html
    ) else (
        echo ⚠️ Resultat inattendu
    )
)

echo.
echo 6. Verification finale des taches...
curl -b "%COOKIE_FILE%" -s -o final_tasks.txt http://localhost:8081/admin/datasets/23/debug-tasks

echo.
echo === TACHES FINALES ===
type final_tasks.txt
echo === FIN DES TACHES FINALES ===

echo.
echo 7. Test de visibilite pour annotateur...
echo Connexion annotateur...
set COOKIE_ANN=%TEMP%\test_ann_debug_cookies.txt
curl -c "%COOKIE_ANN%" -d "username=annotateur1&password=password" -X POST -L -s http://localhost:8081/login

curl -b "%COOKIE_ANN%" -s -o annotator_tasks.html http://localhost:8081/user/tasks

findstr /i "No tasks assigned yet" annotator_tasks.html >nul
if %errorlevel% equ 0 (
    echo ❌ Annotateur: Aucune tache visible
) else (
    echo ✅ Annotateur: Taches detectees
)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del "%COOKIE_ANN%" 2>nul
del assignment_debug.txt 2>nul
del tasks_after_debug.txt 2>nul
del manual_assign_result.html 2>nul
del final_tasks.txt 2>nul
del annotator_tasks.html 2>nul

echo ========================================
echo INTERPRETATION DES RESULTATS
echo ========================================
echo.
echo VERIFIEZ LE DEBUG ASSIGNATION CI-DESSUS:
echo.
echo 1. SI "No couples found":
echo    - Le dataset n'a pas de couples de texte
echo    - Parsez d'abord le dataset
echo.
echo 2. SI "Less than 3 active annotators":
echo    - Creez plus d'annotateurs actifs
echo    - Verifiez que les annotateurs ne sont pas supprimes
echo.
echo 3. SI "Error during assignment test":
echo    - Probleme dans le service AssignTaskToAnnotator
echo    - Verifiez les logs de l'application pour l'exception complete
echo.
echo 4. SI "Assignment completed" MAIS "Tasks created: 0":
echo    - Probleme de transaction ou de sauvegarde
echo    - Verifiez les contraintes de base de donnees
echo.
echo 5. SI "Tasks created: X" (X > 0):
echo    - L'assignation fonctionne !
echo    - Le probleme etait dans la visibilite ou les dates
echo.
echo LOGS A SURVEILLER DANS L'APPLICATION:
echo - "DEBUG: Starting task assignment for dataset: ..."
echo - "DEBUG: Found X text pairs in dataset"
echo - "DEBUG: Creating tasks in database..."
echo - "DEBUG: Created task for annotator: ..."
echo - "DEBUG: Successfully created X tasks"
echo.
echo Si aucun de ces logs n'apparait, le service n'est pas appele.
echo Si ils apparaissent mais "Tasks created: 0", probleme de persistence.
echo.
pause
