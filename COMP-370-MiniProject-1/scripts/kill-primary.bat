@echo off
REM Kill the primary server to test failover

echo =========================================
echo Killing Primary Server (Server 1)
echo =========================================
echo.

set PID=
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :6001 ^| findstr LISTENING') do set PID=%%a

if "%PID%"=="" (
    echo ERROR: Could not find a process listening on port 6001.
    echo Make sure the system is running using run.bat
) else (
    taskkill /PID %PID% /F > nul 2>&1
    if errorlevel 1 (
        echo ERROR: Failed to terminate PID %PID%.
    ) else (
        echo Server 1 terminated! (PID: %PID%)
        echo.
        echo Monitor should detect failure and elect new primary.
        echo Check logs\monitor.log for failover details
    )
)

echo.
echo =========================================
pause
