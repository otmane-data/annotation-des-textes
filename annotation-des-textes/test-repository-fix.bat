@echo off
echo ========================================
echo TEST CORRECTION TACHEREPOSITORY
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
    echo Les methodes manquantes dans TacheRepository ont ete ajoutees !
    echo.
    echo CORRECTIONS APPORTEES:
    echo 1. Ajout de findAnnotatorIdsByDatasetId() dans TacheRepository
    echo 2. Ajout de findDeadlinesByDatasetId() dans TacheRepository
    echo 3. Correction de getDatasetDeadline() dans DatasetServiceImpl
    echo 4. Ajout des imports @Query et @Param
    echo.
    echo METHODES AJOUTEES:
    echo - findAnnotatorIdsByDatasetId: Retourne les IDs des annotateurs assignes
    echo - findDeadlinesByDatasetId: Retourne les deadlines pour un dataset
    echo.
    echo Vous pouvez maintenant redemarrer l'application:
    echo mvnw.cmd spring-boot:run
    
) else (
    echo ❌ ERREURS DE COMPILATION DETECTEES
    echo.
    echo Verifiez les erreurs ci-dessus et corrigez-les.
    echo.
    echo ERREURS POSSIBLES RESTANTES:
    echo 1. Syntaxe JPQL incorrecte
    echo 2. Noms d'entites ou proprietes incorrects
    echo 3. Imports manquants
    echo 4. Conflits de types
)

echo.
echo ========================================
echo VERIFICATION MANUELLE RECOMMANDEE
echo ========================================
echo.
echo 1. Ouvrez TacheRepository.java dans votre IDE
echo 2. Verifiez que les requetes JPQL sont correctes
echo 3. Verifiez que les noms d'entites correspondent
echo 4. Redemarrez l'application pour tester
echo.
echo REQUETES JPQL AJOUTEES:
echo.
echo findAnnotatorIdsByDatasetId:
echo SELECT DISTINCT t.annotateur.id FROM Tache t 
echo WHERE t.dataset.id = :datasetId AND t.annotateur IS NOT NULL
echo.
echo findDeadlinesByDatasetId:
echo SELECT t.dateLimite FROM Tache t 
echo WHERE t.dataset.id = :datasetId AND t.dateLimite IS NOT NULL 
echo ORDER BY t.id ASC
echo.
echo Ces requetes permettent d'eviter les LazyInitializationException
echo en recuperant directement les donnees necessaires.
echo.
pause
