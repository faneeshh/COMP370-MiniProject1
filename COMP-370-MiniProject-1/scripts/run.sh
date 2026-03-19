#!/bin/bash
# Run script for SRMS (Server Redundancy Management System)
# This script compiles all Java sources and starts the system components

echo "========================================="
echo "SRMS - Server Redundancy Management System"
echo "========================================="
echo ""

# Navigate to project root
cd "$(dirname "$0")/.." || exit

# Create output directories
mkdir -p bin logs

echo "[1/5] Compiling Java sources..."
javac -d bin -sourcepath src src/common/*.java src/monitor/*.java src/server/*.java src/client/*.java

if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed!"
    exit 1
fi

echo "[2/5] Starting Monitor..."
java -cp bin monitor.MonitorMain > logs/monitor.log 2>&1 &
MONITOR_PID=$!
echo "Monitor started (PID: $MONITOR_PID)"
sleep 2

echo "[3/5] Starting Server 1 (Primary on port 6001)..."
java -cp bin server.PrimaryServer 1 localhost 6001 > logs/server1.log 2>&1 &
SERVER1_PID=$!
echo "Server 1 started (PID: $SERVER1_PID)"
sleep 1

echo "[4/5] Starting Server 2 (Backup on port 6002)..."
java -cp bin server.BackupServer 2 localhost 6002 > logs/server2.log 2>&1 &
SERVER2_PID=$!
echo "Server 2 started (PID: $SERVER2_PID)"
sleep 1

echo "[5/5] Starting Server 3 (Backup on port 6003)..."
java -cp bin server.BackupServer 3 localhost 6003 > logs/server3.log 2>&1 &
SERVER3_PID=$!
echo "Server 3 started (PID: $SERVER3_PID)"
sleep 1

echo ""
echo "========================================="
echo "SRMS System Started Successfully!"
echo "========================================="
echo "Monitor PID: $MONITOR_PID (Port 5000)"
echo "Server 1 PID: $SERVER1_PID (Port 6001) - PRIMARY"
echo "Server 2 PID: $SERVER2_PID (Port 6002) - BACKUP"
echo "Server 3 PID: $SERVER3_PID (Port 6003) - BACKUP"
echo ""
echo "Logs are available in the logs/ directory"
echo "To view monitor logs: tail -f logs/monitor.log"
echo ""
echo "To start a client:"
echo "  java -cp bin client.ClientMain <clientId> [interactive]"
echo ""
echo "To stop all servers:"
echo "  kill $MONITOR_PID $SERVER1_PID $SERVER2_PID $SERVER3_PID"
echo "========================================="

# Save PIDs to file for other scripts
echo "$MONITOR_PID" > logs/monitor.pid
echo "$SERVER1_PID" > logs/server1.pid
echo "$SERVER2_PID" > logs/server2.pid
echo "$SERVER3_PID" > logs/server3.pid
 
