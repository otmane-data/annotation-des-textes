@echo off
echo ========================================
echo TEST CORRECTION PERSISTENCE DES TACHES
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
set COOKIE_FILE=%TEMP%\test_persistence_cookies.txt
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo.
echo 3. Test de l'assignation automatique...
echo Test automatique avec debug complet...
curl -b "%COOKIE_FILE%" -s -o assignment_debug.txt http://localhost:8081/admin/datasets/23/debug-assignment

echo.
echo === RESULTAT DU DEBUG ASSIGNATION ===
type assignment_debug.txt
echo === FIN DU DEBUG ===

echo.
echo 4. Verification des taches apres assignation automatique...
curl -b "%COOKIE_FILE%" -s -o tasks_after_auto.txt http://localhost:8081/admin/datasets/23/debug-tasks

echo.
echo === TACHES APRES ASSIGNATION AUTOMATIQUE ===
type tasks_after_auto.txt
echo === FIN DES TACHES ===

echo.
echo 5. Test d'assignation manuelle...
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
set COOKIE_ANN=%TEMP%\test_ann_persistence_cookies.txt
curl -c "%COOKIE_ANN%" -d "username=annotateur1&password=password" -X POST -L -s http://localhost:8081/login

curl -b "%COOKIE_ANN%" -s -o annotator_tasks.html http://localhost:8081/user/tasks

findstr /i "No tasks assigned yet" annotator_tasks.html >nul
if %errorlevel% equ 0 (
    echo ❌ Annotateur: Aucune tache visible
) else (
    echo ✅ Annotateur: Taches detectees !
fi

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del "%COOKIE_ANN%" 2>nul
del assignment_debug.txt 2>nul
del tasks_after_auto.txt 2>nul
del manual_assign_result.html 2>nul
del final_tasks.txt 2>nul
del annotator_tasks.html 2>nul

echo ========================================
echo CORRECTIONS APPORTEES
echo ========================================
echo.
echo PROBLEMES CORRIGES:
echo ✅ Cascade CascadeType.ALL remplace par PERSIST + MERGE
echo ✅ Sauvegarde en deux etapes (tache puis couples)
echo ✅ Ajout de entityManager.flush() pour forcer la persistence
echo ✅ Gestion d'erreur detaillee pour chaque sauvegarde
echo ✅ Logs de debug ameliores avec IDs des taches
echo.
echo CHANGEMENTS TECHNIQUES:
echo 1. Entite Tache: cascade = {CascadeType.PERSIST, CascadeType.MERGE}
echo 2. Service: Sauvegarde en deux etapes
echo 3. Service: Ajout de @PersistenceContext EntityManager
echo 4. Service: entityManager.flush() apres chaque sauvegarde
echo 5. Service: Try-catch individuel pour chaque tache
echo.
echo VERIFICATION MANUELLE:
echo 1. Surveillez les logs de l'application pour:
echo    - "DEBUG: Saved task with ID: X"
echo    - "DEBUG: Successfully created task ID X"
echo    - Aucune exception lors de la sauvegarde
echo.
echo 2. Verifiez que "Total tasks in database" > 0
echo.
echo 3. Connectez-vous en tant qu'annotateur et verifiez
echo    que les taches apparaissent sur /user/tasks
echo.
echo Si "Tasks created: X" (X > 0) dans le debug,
echo le probleme de persistence est resolu !
echo.
pause
