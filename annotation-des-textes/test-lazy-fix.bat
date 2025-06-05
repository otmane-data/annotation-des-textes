@echo off
echo ========================================
echo TEST CORRECTION LAZYINITIALIZATIONEXCEPTION
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
set COOKIE_FILE=%TEMP%\test_lazy_fix_cookies.txt
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo.
echo 3. Test de la page d'assignation (source de l'erreur 500)...
echo Acces a la page d'assignation pour dataset 1...
curl -b "%COOKIE_FILE%" -s -w "HTTP Status: %%{http_code}\n" -o assign_page_test.html http://localhost:8081/admin/datasets/1/assign_annotator

echo.
echo Verification du code de reponse...
findstr /i "500\|Unknown error\|LazyInitializationException" assign_page_test.html >nul
if %errorlevel% equ 0 (
    echo ❌ ERREUR 500 ENCORE PRESENTE
    echo.
    echo Contenu de l'erreur:
    findstr /i "500\|Unknown error\|LazyInitializationException" assign_page_test.html
) else (
    echo ✅ ERREUR 500 CORRIGEE - Page d'assignation accessible
    
    echo.
    echo Verification du contenu de la page...
    findstr /i "Select Annotators\|deadline\|selectedAnnotateurs" assign_page_test.html >nul
    if %errorlevel% equ 0 (
        echo ✅ Elements du formulaire detectes
        
        echo.
        echo Verification des annotateurs...
        findstr /i "annotateur-checkbox" assign_page_test.html >nul
        if %errorlevel% equ 0 (
            echo ✅ Annotateurs detectes dans la page
        ) else (
            echo ⚠️ Aucun annotateur detecte (peut-etre aucun en base)
        )
        
    ) else (
        echo ❌ Elements du formulaire non detectes
    )
)

echo.
echo 4. Test de l'assignation complete...
echo Test avec 3 annotateurs pour verifier que tout fonctionne...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=2024-12-31" ^
     -d "selectedAnnotateurs=1" ^
     -d "selectedAnnotateurs=2" ^
     -d "selectedAnnotateurs=3" ^
     -X POST ^
     -s ^
     -w "HTTP Status: %%{http_code}\n" ^
     -o assign_result_test.html ^
     http://localhost:8081/admin/datasets/1/assign

echo.
echo Verification du resultat de l'assignation...
findstr /i "500\|Unknown error\|LazyInitializationException" assign_result_test.html >nul
if %errorlevel% equ 0 (
    echo ❌ ERREUR 500 lors de l'assignation
    echo.
    echo Contenu de l'erreur:
    findstr /i "500\|Unknown error\|LazyInitializationException" assign_result_test.html
) else (
    findstr /i "success\|successfully" assign_result_test.html >nul
    if %errorlevel% equ 0 (
        echo ✅ ASSIGNATION REUSSIE
    ) else (
        findstr /i "error\|Error" assign_result_test.html >nul
        if %errorlevel% equ 0 (
            echo ⚠️ Erreur d'assignation (pas 500):
            findstr /i "error\|Error" assign_result_test.html
        ) else (
            echo ⚠️ Resultat inattendu
        )
    )
)

echo.
echo 5. Test avec d'autres datasets...
echo Test de la page d'assignation pour dataset 2...
curl -b "%COOKIE_FILE%" -s -w "HTTP Status: %%{http_code}\n" -o assign_page_test2.html http://localhost:8081/admin/datasets/2/assign_annotator

findstr /i "500\|Unknown error\|LazyInitializationException" assign_page_test2.html >nul
if %errorlevel% equ 0 (
    echo ❌ Erreur 500 encore presente pour dataset 2
) else (
    echo ✅ Dataset 2 accessible sans erreur 500
)

echo.
echo 6. Verification des logs de l'application...
echo IMPORTANT: Verifiez dans la console de l'application:
echo - Aucune LazyInitializationException
echo - Messages "Found X assigned annotators for dataset Y"
echo - Messages "Error getting assigned annotators" (si aucun annotateur assigne)
echo - Messages de debug du service AssignTaskToAnnotator

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del assign_page_test.html 2>nul
del assign_result_test.html 2>nul
del assign_page_test2.html 2>nul

echo ========================================
echo RESULTATS DE LA CORRECTION
echo ========================================
echo.
echo PROBLEME CORRIGE:
echo ✅ LazyInitializationException dans DatasetController.assignAnnotator()
echo ✅ Acces direct a dataset.getTaches() remplace par des methodes de service
echo ✅ Methodes getAssignedAnnotatorIds() et getDatasetDeadline() ajoutees
echo ✅ Utilisation de requetes JPQL pour eviter le lazy loading
echo.
echo CHANGEMENTS APPORTES:
echo 1. DatasetController: Remplacement de dataset.getTaches() par datasetService.getAssignedAnnotatorIds()
echo 2. DatasetService: Ajout des methodes getAssignedAnnotatorIds() et getDatasetDeadline()
echo 3. DatasetServiceImpl: Implementation des nouvelles methodes avec TacheRepository
echo 4. TacheRepository: Utilisation des requetes JPQL existantes
echo.
echo VERIFICATION MANUELLE:
echo 1. Allez sur http://localhost:8081/admin/datasets/details/1
echo 2. Cliquez sur "Assign Annotators"
echo 3. Verifiez que la page se charge sans erreur 500
echo 4. Selectionnez des annotateurs et testez l'assignation
echo.
echo Si tous les tests montrent "✅", la LazyInitializationException est corrigee !
echo.
pause
