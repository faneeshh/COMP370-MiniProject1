#!/bin/bash
# Stop all SRMS components

echo "========================================="
echo "Stopping SRMS System"
echo "========================================="
echo ""

cd "$(dirname "$0")/.." || exit

# Check for PID files and kill processes
for component in monitor server1 server2 server3; do
    PID_FILE="logs/${component}.pid"
    
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        
        if ps -p $PID > /dev/null 2>&1; then
            echo "Stopping $component (PID: $PID)..."
            kill $PID
            rm "$PID_FILE"
        else
            echo "$component (PID: $PID) not running"
            rm "$PID_FILE"
        fi
    else
        echo "No PID file for $component"
    fi
done

echo ""
echo "All SRMS components stopped"
echo "========================================="
