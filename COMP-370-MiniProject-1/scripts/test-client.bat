@echo off
REM Test client script - demonstrates SRMS functionality

echo =========================================
echo SRMS Test Client
echo =========================================
echo.

cd /d "%~dp0\.."

if not exist bin (
    echo ERROR: Please compile the project first using scripts\run.bat
    exit /b 1
)

set CLIENT_ID=%1
if "%CLIENT_ID%"=="" set CLIENT_ID=test-client-1

echo Starting client: %CLIENT_ID%
echo Client will send 5 test requests to the primary server
echo.
echo Press Ctrl+C to stop
echo =========================================
echo.

java -cp bin client.ClientMain %CLIENT_ID%
pause
