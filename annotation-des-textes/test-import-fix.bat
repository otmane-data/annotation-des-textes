@echo off
echo ========================================
echo TEST CORRECTION IMPORT TACHE
echo ========================================
echo.

echo 1. Verification de la compilation...
cd /d "C:\Users\otman\OneDrive\Bureau\annotation-des-textes"

echo Compilation en cours...
call mvnw.cmd compile

if %errorlevel% equ 0 (
    echo ✅ COMPILATION REUSSIE
    echo.
    echo L'import de la classe Tache a ete ajoute avec succes !
    echo.
    echo CORRECTION APPORTEE:
    echo import com.otmane.annotation_des_textes.entities.Tache;
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
    echo 1. Autres imports manquants
    echo 2. Methodes non implementees
    echo 3. Types incompatibles
    echo 4. Erreurs de syntaxe
)

echo.
echo ========================================
echo PROCHAINES ETAPES
echo ========================================
echo.
echo 1. Redemarrez l'application
echo 2. Testez l'assignation d'annotateurs
echo 3. Utilisez l'endpoint de debug pour verifier les taches:
echo    http://localhost:8081/admin/datasets/[ID]/debug-tasks
echo 4. Verifiez que les taches apparaissent pour les annotateurs
echo.
echo L'endpoint de debug vous montrera:
echo - Nombre total de taches creees
echo - Details de chaque tache (ID, annotateur, deadline, couples)
echo - Informations pour diagnostiquer le probleme d'assignation
echo.
pause
