@echo off
REM Stop all SRMS components

echo =========================================
echo Stopping SRMS System
echo =========================================
echo.

REM Kill all SRMS windows
taskkill /FI "WINDOWTITLE eq SRMS-*" /F > nul 2>&1

if errorlevel 1 (
    echo No SRMS processes found running
) else (
    echo All SRMS components stopped
)

echo.
echo =========================================
pause
