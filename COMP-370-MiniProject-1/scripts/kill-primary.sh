#!/bin/bash
# Kill the primary server to test failover

echo "=========================================="
echo "Killing Primary Server (Server 1)"
echo "=========================================="
echo ""

cd "$(dirname "$0")/.." || exit

if [ -f logs/server1.pid ]; then
    SERVER1_PID=$(cat logs/server1.pid)
    
    if ps -p $SERVER1_PID > /dev/null 2>&1; then
        echo "Terminating Server 1 (PID: $SERVER1_PID)..."
        kill -9 $SERVER1_PID
        
        echo "Server 1 terminated!"
        echo ""
        echo "Monitor should detect failure and elect new primary."
        echo "Check logs/monitor.log for failover details:"
        echo "  tail -f logs/monitor.log"
        echo ""
        
        # Remove PID file
        rm logs/server1.pid
    else
        echo "Server 1 (PID: $SERVER1_PID) is not running."
    fi
else
    echo "ERROR: logs/server1.pid not found."
    echo "Make sure the system is running (use ./scripts/run.sh)"
fi

echo "==========================================" 
