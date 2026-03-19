#!/bin/bash
# Test client script - demonstrates SRMS functionality

echo "========================================="
echo "SRMS Test Client"
echo "========================================="
echo ""

cd "$(dirname "$0")/.." || exit

if [ ! -d "bin" ]; then
    echo "ERROR: Please compile the project first using ./scripts/run.sh"
    exit 1
fi

CLIENT_ID=${1:-test-client-1}

echo "Starting client: $CLIENT_ID"
echo "Client will send 5 test requests to the primary server"
echo ""
echo "Press Ctrl+C to stop"
echo "========================================="
echo ""

java -cp bin client.ClientMain "$CLIENT_ID"
