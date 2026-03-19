#!/bin/bash
# Simulate heartbeat delay by suspending a server process temporarily

echo "=========================================="
echo "Simulating Heartbeat Delay"
echo "=========================================="
echo ""

cd "$(dirname "$0")/.." || exit

# Default to Server 1
SERVER_NUM=${1:-1}
DELAY_SECONDS=${2:-15}

PID_FILE="logs/server${SERVER_NUM}.pid"

if [ -f "$PID_FILE" ]; then
    SERVER_PID=$(cat "$PID_FILE")
    
    if ps -p $SERVER_PID > /dev/null 2>&1; then
        echo "Suspending Server $SERVER_NUM (PID: $SERVER_PID) for $DELAY_SECONDS seconds..."
        echo "This will cause heartbeats to stop temporarily."
        echo ""
        
        # Suspend the process (SIGSTOP)
        kill -STOP $SERVER_PID
        echo "Server $SERVER_NUM suspended at $(date '+%Y-%m-%d %H:%M:%S')"
        
        # Wait for specified delay
        sleep $DELAY_SECONDS
        
        # Resume the process (SIGCONT)
        kill -CONT $SERVER_PID
        echo "Server $SERVER_NUM resumed at $(date '+%Y-%m-%d %H:%M:%S')"
        echo ""
        echo "Check logs to see if failover was triggered:"
        echo "  tail -f logs/monitor.log"
        echo ""
    else
        echo "Server $SERVER_NUM (PID: $SERVER_PID) is not running."
    fi
else
    echo "ERROR: $PID_FILE not found."
    echo "Make sure the system is running (use ./scripts/run.sh)"
    echo ""
    echo "Usage: $0 [server_number] [delay_seconds]"
    echo "  server_number: 1, 2, or 3 (default: 1)"
    echo "  delay_seconds: delay duration (default: 15)"
fi

echo "==========================================" 
