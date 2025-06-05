@echo off
echo ========================================
echo TEST INJECTION TACHESERVICE
echo ========================================
echo.

echo 1. Verification de la compilation...
cd /d "C:\Users\otman\OneDrive\Bureau\annotation-des-textes"

echo Compilation en cours...
call mvnw.cmd compile

if %errorlevel% equ 0 (
    echo ✅ COMPILATION REUSSIE
    echo.
    echo L'injection du TacheService a ete ajoutee avec succes !
    echo.
    echo CORRECTIONS APPORTEES:
    echo 1. Ajout de "private final TacheService tacheService;" dans DatasetController
    echo 2. Ajout du parametre TacheService dans le constructeur
    echo 3. Ajout de "this.tacheService = tacheService;" dans le constructeur
    echo 4. Import deja present via "import com.otmane.annotation_des_textes.services.*;"
    echo.
    echo Vous pouvez maintenant redemarrer l'application:
    echo mvnw.cmd spring-boot:run
    echo.
    echo Et tester l'endpoint de debug:
    echo http://localhost:8081/admin/datasets/23/debug-tasks
    
) else (
    echo ❌ ERREURS DE COMPILATION DETECTEES
    echo.
    echo Verifiez les erreurs ci-dessus et corrigez-les.
    echo.
    echo ERREURS POSSIBLES RESTANTES:
    echo 1. TacheService non trouve
    echo 2. Dependance circulaire
    echo 3. Autres erreurs de syntaxe
)

echo.
echo ========================================
echo PROCHAINES ETAPES
echo ========================================
echo.
echo 1. Redemarrez l'application
echo 2. Testez l'assignation d'annotateurs:
echo    - Allez sur http://localhost:8081/admin/datasets/details/23
echo    - Cliquez sur "Assign Annotators"
echo    - Selectionnez 3+ annotateurs
echo    - Definissez une deadline
echo    - Cliquez sur "Assign Selected Annotators"
echo.
echo 3. Utilisez l'endpoint de debug pour verifier les taches:
echo    http://localhost:8081/admin/datasets/23/debug-tasks
echo.
echo 4. Verifiez que les taches apparaissent pour les annotateurs:
echo    - Connectez-vous en tant qu'annotateur
echo    - Allez sur la page des taches assignees
echo.
echo L'endpoint de debug vous montrera:
echo - Nombre total de taches creees
echo - Details de chaque tache (ID, annotateur, deadline, couples)
echo - Informations pour diagnostiquer le probleme d'assignation
echo.
echo Si le debug montre "Total tasks found: 0" apres assignation,
echo le probleme est dans le service AssignTaskToAnnotator.
echo.
echo Si il montre des taches mais l'annotateur ne les voit pas,
echo le probleme est dans l'interface annotateur ou getValidTasksForAnnotateur().
echo.
pause
