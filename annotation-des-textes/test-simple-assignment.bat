@echo off
echo ========================================
echo TEST SIMPLE ASSIGNATION
echo ========================================
echo.

echo 1. Connexion admin...
curl -s -c temp_cookies.txt -d "username=admin&password=admin" -X POST http://localhost:8081/login

echo.
echo 2. Test assignation automatique avec debug detaille...
echo Surveillez ATTENTIVEMENT les logs de l'application !
echo.
curl -s -b temp_cookies.txt http://localhost:8081/admin/datasets/23/debug-assignment

echo.
echo 3. Verification des taches en base...
curl -s -b temp_cookies.txt http://localhost:8081/admin/datasets/23/debug-tasks

echo.
echo 4. Test assignation manuelle...
curl -s -b temp_cookies.txt ^
     -d "deadline=2025-12-31" ^
     -d "selectedAnnotateurs=1" ^
     -d "selectedAnnotateurs=2" ^
     -d "selectedAnnotateurs=3" ^
     -X POST ^
     http://localhost:8081/admin/datasets/23/assign

echo.
echo 5. Verification finale...
curl -s -b temp_cookies.txt http://localhost:8081/admin/datasets/23/debug-tasks

del temp_cookies.txt

echo.
echo ========================================
echo INSTRUCTIONS
echo ========================================
echo.
echo SURVEILLEZ LES LOGS DE L'APPLICATION POUR:
echo.
echo ✅ LOGS DE SUCCES:
echo - "DEBUG: About to create task entity..."
echo - "DEBUG: Task entity created"
echo - "DEBUG: Setting annotator: X - NomAnnotateur"
echo - "DEBUG: Setting dataset: X - NomDataset"
echo - "DEBUG: About to save task to repository..."
echo - "DEBUG: Task saved with ID: X"
echo - "DEBUG: Task exists in DB: true"
echo - "DEBUG: Final task found with X couples"
echo.
echo ❌ LOGS D'ERREUR A SURVEILLER:
echo - "ERROR: Failed to save task"
echo - "ERROR: Final task not found in database!"
echo - Exceptions de type ConstraintViolationException
echo - Exceptions de type DataIntegrityViolationException
echo - Exceptions de type LazyInitializationException
echo.
echo 📊 RESULTATS ATTENDUS:
echo - Si vous voyez "Tasks created: 3" → Assignation reussie
echo - Si vous voyez "Total tasks in database: 3" → Persistence reussie
echo - Si vous voyez "Tasks created: 0" → Probleme d'assignation
echo.
echo 🔧 ACTIONS SELON LES RESULTATS:
echo.
echo Si "Task entity created" mais pas "Task saved with ID":
echo → Probleme lors de la sauvegarde (contraintes, validation)
echo.
echo Si "Task saved with ID" mais "Task exists in DB: false":
echo → Probleme de transaction (rollback)
echo.
echo Si "Task exists in DB: true" mais "Final task not found":
echo → Probleme lors de l'ajout des couples
echo.
echo Si tout semble OK mais table vide:
echo → Probleme de transaction ou de commit
echo.
pause
