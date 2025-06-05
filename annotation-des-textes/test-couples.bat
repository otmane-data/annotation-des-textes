@echo off
echo ========================================
echo DIAGNOSTIC DES COUPLES DE TEXTE
echo ========================================
echo.

echo 1. Recherche du JAR MySQL...
set MYSQL_JAR=%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar

if not exist "%MYSQL_JAR%" (
    echo JAR MySQL non trouve, tentative de telechargement...
    call mvnw.cmd dependency:copy-dependencies -DoutputDirectory=lib
    set MYSQL_JAR=lib\mysql-connector-j-8.3.0.jar
)

if exist "%MYSQL_JAR%" (
    echo JAR MySQL trouve: %MYSQL_JAR%
    echo.
    echo 2. Compilation du diagnostic...
    javac -cp "%MYSQL_JAR%" TestDatasetCouples.java
    if %errorlevel% equ 0 (
        echo Compilation reussie
        echo.
        echo 3. Execution du diagnostic...
        java -cp ".;%MYSQL_JAR%" TestDatasetCouples
    ) else (
        echo Erreur de compilation
    )
) else (
    echo JAR MySQL non trouve. Veuillez verifier l'installation de Maven.
)

echo.
echo ========================================
echo FIN DU DIAGNOSTIC
echo ========================================
pause
