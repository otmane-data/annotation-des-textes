@echo off
echo Test rapide de l'assignation...

curl -s -c temp_cookies.txt -d "username=admin&password=admin" -X POST http://localhost:8081/login

echo.
echo === DEBUG ASSIGNATION ===
curl -s -b temp_cookies.txt http://localhost:8081/admin/datasets/23/debug-assignment

echo.
echo === TACHES EN BASE ===
curl -s -b temp_cookies.txt http://localhost:8081/admin/datasets/23/debug-tasks

del temp_cookies.txt
pause
