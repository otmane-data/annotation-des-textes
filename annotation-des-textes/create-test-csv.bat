@echo off
echo ========================================
echo CREATION FICHIER CSV DE TEST
echo ========================================
echo.

echo Creation du fichier test_dataset.csv...

echo text1,text2 > test_dataset.csv
echo "Hello World","Bonjour Monde" >> test_dataset.csv
echo "Good morning","Bon matin" >> test_dataset.csv
echo "How are you?","Comment allez-vous?" >> test_dataset.csv
echo "Thank you","Merci" >> test_dataset.csv
echo "Goodbye","Au revoir" >> test_dataset.csv
echo "Yes","Oui" >> test_dataset.csv
echo "No","Non" >> test_dataset.csv
echo "Please","S'il vous plait" >> test_dataset.csv
echo "Excuse me","Excusez-moi" >> test_dataset.csv
echo "I love you","Je t'aime" >> test_dataset.csv

echo ✅ Fichier test_dataset.csv cree avec 10 couples de texte
echo.

echo Contenu du fichier:
type test_dataset.csv

echo.
echo ========================================
echo INSTRUCTIONS POUR TESTER
echo ========================================
echo.
echo 1. Redemarrez l'application: mvnw.cmd spring-boot:run
echo 2. Allez sur http://localhost:8081/admin/datasets/add-simple
echo 3. Remplissez le formulaire:
echo    - Name: Test Dataset CSV
echo    - Description: Test avec fichier CSV
echo    - File: Selectionnez test_dataset.csv
echo    - Classes: positive;negative
echo 4. Cliquez sur "Create Dataset"
echo 5. Verifiez les logs dans la console pour:
echo    - "Starting async parsing for dataset..."
echo    - "Parsing CSV file..."
echo    - "Successfully parsed X rows from dataset file"
echo.
pause
