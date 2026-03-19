@echo off
REM Run script for SRMS (Server Redundancy Management System)
REM This script compiles all Java sources and starts the system components

echo =========================================
echo SRMS - Server Redundancy Management System
echo =========================================
echo.

cd /d "%~dp0\.."

REM Create output directories
if not exist bin mkdir bin
if not exist logs mkdir logs

echo [1/5] Compiling Java sources...
javac -d bin -sourcepath src src\common\*.java src\monitor\*.java src\server\*.java src\client\*.java

if errorlevel 1 (
    echo ERROR: Compilation failed!
    exit /b 1
)

echo [2/5] Starting Monitor...
start "SRMS-Monitor" cmd /c "java -cp bin monitor.MonitorMain"
timeout /t 2 /nobreak > nul

echo [3/5] Starting Server 1 (Primary on port 6001)...
start "SRMS-Server1-Primary" cmd /c "java -cp bin server.PrimaryServer 1 localhost 6001"
timeout /t 1 /nobreak > nul

echo [4/5] Starting Server 2 (Backup on port 6002)...
start "SRMS-Server2-Backup" cmd /c "java -cp bin server.BackupServer 2 localhost 6002"
timeout /t 1 /nobreak > nul

echo [5/5] Starting Server 3 (Backup on port 6003)...
start "SRMS-Server3-Backup" cmd /c "java -cp bin server.BackupServer 3 localhost 6003"
timeout /t 1 /nobreak > nul

echo.
echo =========================================
echo SRMS System Started Successfully!
echo =========================================
echo Monitor running on Port 5000
echo Server 1 running on Port 6001 - PRIMARY
echo Server 2 running on Port 6002 - BACKUP
echo Server 3 running on Port 6003 - BACKUP
echo.
echo Logs are available in the logs\ directory
echo To view monitor logs: type logs\monitor.log
echo.
echo To start a client:
echo   java -cp bin client.ClientMain ^<clientId^> [interactive]
echo.
echo To stop all servers:
echo   Close the command windows or use Task Manager
echo =========================================
pause
