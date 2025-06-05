@echo off
echo ========================================
echo TEST CORRECTION ERREUR DE DATE
echo ========================================
echo.

echo 1. Verification de la compilation...
cd /d "C:\Users\otman\OneDrive\Bureau\annotation-des-textes"

echo Compilation en cours...
call mvnw.cmd compile

if %errorlevel% neq 0 (
    echo ❌ Erreur de compilation
    pause
    exit /b 1
)

echo ✅ Compilation reussie
echo.

echo 2. Redemarrage de l'application...
echo IMPORTANT: Redemarrez l'application manuellement avec:
echo mvnw.cmd spring-boot:run
echo.
echo Puis appuyez sur une touche pour continuer le test...
pause

echo.
echo 3. Test de l'assignation avec date...
echo Connexion admin...
set COOKIE_FILE=%TEMP%\test_date_cookies.txt
curl -c "%COOKIE_FILE%" -d "username=admin&password=admin" -X POST -L -s http://localhost:8081/login

echo.
echo Test d'assignation avec date au format yyyy-MM-dd...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=2025-06-03" ^
     -d "selectedAnnotateurs=1" ^
     -d "selectedAnnotateurs=2" ^
     -d "selectedAnnotateurs=3" ^
     -X POST ^
     -s ^
     -w "HTTP Status: %%{http_code}\n" ^
     -o assign_date_result.html ^
     http://localhost:8081/admin/datasets/23/assign

echo.
echo Verification du resultat...
findstr /i "MethodArgumentTypeMismatchException\|Failed to convert\|Date" assign_date_result.html >nul
if %errorlevel% equ 0 (
    echo ❌ ERREUR DE CONVERSION DE DATE ENCORE PRESENTE
    echo.
    echo Contenu de l'erreur:
    findstr /i "MethodArgumentTypeMismatchException\|Failed to convert\|Date" assign_date_result.html
) else (
    findstr /i "success\|successfully" assign_date_result.html >nul
    if %errorlevel% equ 0 (
        echo ✅ ASSIGNATION REUSSIE - Erreur de date corrigee
    ) else (
        findstr /i "error\|Error" assign_date_result.html >nul
        if %errorlevel% equ 0 (
            echo ⚠️ Autre erreur detectee (pas de conversion de date):
            findstr /i "error\|Error" assign_date_result.html
        ) else (
            echo ⚠️ Resultat inattendu
            echo Premiers 200 caracteres:
            powershell -command "Get-Content assign_date_result.html -Raw | ForEach-Object { $_.Substring(0, [Math]::Min(200, $_.Length)) }"
        )
    )
)

echo.
echo 4. Test avec differents formats de date...
echo Test avec date au format dd/MM/yyyy (devrait echouer)...
curl -b "%COOKIE_FILE%" ^
     -d "deadline=03/06/2025" ^
     -d "selectedAnnotateurs=1" ^
     -d "selectedAnnotateurs=2" ^
     -d "selectedAnnotateurs=3" ^
     -X POST ^
     -s ^
     -w "HTTP Status: %%{http_code}\n" ^
     -o assign_date_wrong.html ^
     http://localhost:8081/admin/datasets/23/assign

findstr /i "MethodArgumentTypeMismatchException\|Failed to convert" assign_date_wrong.html >nul
if %errorlevel% equ 0 (
    echo ✅ Format incorrect rejete comme attendu
) else (
    echo ⚠️ Format incorrect accepte (inattendu)
)

echo.
echo Nettoyage...
del "%COOKIE_FILE%" 2>nul
del assign_date_result.html 2>nul
del assign_date_wrong.html 2>nul

echo ========================================
echo RESULTATS DE LA CORRECTION
echo ========================================
echo.
echo PROBLEME CORRIGE:
echo ✅ Erreur de conversion de date dans TaskController
echo ✅ Ajout de @DateTimeFormat(pattern = "yyyy-MM-dd")
echo ✅ Import de org.springframework.format.annotation.DateTimeFormat
echo.
echo CORRECTION APPORTEE:
echo @RequestParam("deadline") @DateTimeFormat(pattern = "yyyy-MM-dd") Date deadline
echo.
echo Cette annotation indique a Spring comment convertir la string
echo du formulaire HTML (format yyyy-MM-dd) en objet Date Java.
echo.
echo VERIFICATION MANUELLE:
echo 1. Allez sur http://localhost:8081/admin/datasets/details/23
echo 2. Cliquez sur "Assign Annotators"
echo 3. Selectionnez 3+ annotateurs
echo 4. Definissez une date (format yyyy-MM-dd)
echo 5. Cliquez sur "Assign Selected Annotators"
echo 6. Verifiez qu'il n'y a plus d'erreur de conversion de date
echo.
echo Si le test montre "✅ ASSIGNATION REUSSIE", 
echo l'erreur de conversion de date est corrigee !
echo.
pause
