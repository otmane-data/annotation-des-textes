@echo off
echo ========================================
echo TEST CORRECTION METHODES TACHESERVICE
echo ========================================
echo.

echo 1. Verification de la compilation...
cd /d "C:\Users\otman\OneDrive\Bureau\annotation-des-textes"

echo Compilation en cours...
call mvnw.cmd compile

if %errorlevel% equ 0 (
    echo ✅ COMPILATION REUSSIE
    echo.
    echo Les methodes manquantes dans TacheService ont ete ajoutees !
    echo.
    echo METHODES AJOUTEES:
    echo - save(Tache tache) : Sauvegarde une tache
    echo - existsById(Long id) : Verifie si une tache existe
    echo - count() : Compte le nombre total de taches
    echo - findTasksByDatasetId(Long datasetId) : Trouve les taches par dataset
    echo.
    echo Vous pouvez maintenant redemarrer l'application:
    echo mvnw.cmd spring-boot:run
    echo.
    echo Et tester les endpoints de debug:
    echo - http://localhost:8081/admin/datasets/23/test-simple-task
    echo - http://localhost:8081/admin/datasets/23/debug-assignment
    
) else (
    echo ❌ ERREURS DE COMPILATION DETECTEES
    echo.
    echo Verifiez les erreurs ci-dessus et corrigez-les.
)

echo.
echo ========================================
echo PROCHAINES ETAPES
echo ========================================
echo.
echo 1. Redemarrez l'application
echo 2. Testez le test simple de creation de tache:
echo    http://localhost:8081/admin/datasets/23/test-simple-task
echo.
echo 3. Si le test simple fonctionne, testez l'assignation complete:
echo    http://localhost:8081/admin/datasets/23/debug-assignment
echo.
echo 4. Verifiez les taches en base:
echo    http://localhost:8081/admin/datasets/23/debug-tasks
echo.
echo 5. Testez l'assignation manuelle via l'interface web
echo.
echo LOGS A SURVEILLER:
echo - "Task saved with ID: X"
echo - "Task exists in DB: true"
echo - "Total tasks in database: X"
echo.
echo Si le test simple fonctionne mais l'assignation complete echoue,
echo le probleme est dans le service AssignTaskToAnnotator.
echo.
echo Si le test simple echoue aussi, le probleme est plus fondamental
echo (contraintes de base de donnees, configuration JPA, etc.).
echo.
pause
