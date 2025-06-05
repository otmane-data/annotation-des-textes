@echo off
echo ========================================
echo TEST COMPILATION DATASETSERVICEIMPL
echo ========================================
echo.

echo 1. Verification de la compilation...
echo Compilation en cours...

cd /d "C:\Users\otman\OneDrive\Bureau\annotation-des-textes"

echo.
echo Execution de mvn compile...
call mvnw.cmd compile

if %errorlevel% equ 0 (
    echo ✅ COMPILATION REUSSIE
    echo.
    echo Les erreurs dans DatasetServiceImpl ont ete corrigees !
    echo.
    echo CORRECTIONS APPORTEES:
    echo 1. Suppression de @Autowired sur les champs final
    echo 2. Ajout de l'import java.util.Date
    echo 3. Suppression de la declaration dupliquee de CoupleTextService
    echo 4. Reorganisation des injections de dependances
    echo.
    echo Vous pouvez maintenant redemarrer l'application:
    echo mvnw.cmd spring-boot:run
    
) else (
    echo ❌ ERREURS DE COMPILATION DETECTEES
    echo.
    echo Verifiez les erreurs ci-dessus et corrigez-les.
    echo.
    echo ERREURS POSSIBLES RESTANTES:
    echo 1. Imports manquants
    echo 2. Methodes non implementees
    echo 3. Types incompatibles
    echo 4. Dependances circulaires
)

echo.
echo ========================================
echo VERIFICATION MANUELLE RECOMMANDEE
echo ========================================
echo.
echo 1. Ouvrez DatasetServiceImpl.java dans votre IDE
echo 2. Verifiez qu'il n'y a plus d'erreurs de compilation
echo 3. Verifiez que toutes les dependances sont correctement injectees
echo 4. Redemarrez l'application pour tester
echo.
pause
