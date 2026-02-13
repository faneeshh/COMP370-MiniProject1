# COMP 370 – Mini Project 1: Server Redundancy Management System

This repository contains our implementation of the Server Redundancy Management System (SRMS) for COMP 370.  
The system simulates a primary–backup server cluster with a monitor and client, including heartbeats, failover, and testing scenarios.

## Project Structure

```
COMP-370-MiniProject-1/
├── src/
│   ├── common/           # Shared classes (Message, ServerInfo, MessageType)
│   ├── monitor/          # Monitor logic (heartbeat tracking, failover)
│   ├── server/           # Server implementations (Primary, Backup, HeartbeatSender)
│   └── client/           # Client implementation (discovery, requests)
├── scripts/              # Run and test scripts
│   ├── run.sh / run.bat
│   ├── kill-primary.sh / kill-primary.bat
│   └── delay-heartbeat.sh / delay-heartbeat.bat
├── bin/                  # Compiled classes (generated)
├── logs/                 # Log files (generated)
└── docs/                 # UML diagrams and documentation
```

## System Architecture

### Components

1. **Monitor** (Port 5000)
   - Receives heartbeats from all servers
   - Detects server failures via timeout
   - Elects new primary (lowest ID) when primary fails
   - Provides primary discovery for clients

2. **Server Processes**
   - **Primary Server** (Port 6001): Handles client requests
   - **Backup Servers** (Ports 6002, 6003): Standby, ready for promotion
   - All servers send heartbeats every 3 seconds
   - Failure detected after 10 seconds without heartbeat

3. **Client**
   - Discovers primary via monitor
   - Sends PROCESS requests to primary
   - Automatically reconnects after failover

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- For Linux/Mac: Bash shell
- For Windows: Command Prompt or PowerShell

## Quick Start

### On Windows

```batch
cd COMP-370-MiniProject-1
scripts\run.bat
```

### On Linux/Mac

```bash
cd COMP-370-MiniProject-1
chmod +x scripts/*.sh
./scripts/run.sh
```

This will:

1. Compile all Java sources to `bin/`
2. Start the Monitor on port 5000
3. Start Server 1 (Primary) on port 6001
4. Start Server 2 (Backup) on port 6002
5. Start Server 3 (Backup) on port 6003

All output is logged to `logs/` directory.

## Running a Client

Open a new terminal and run:

```bash
# Automated mode (sends predefined test requests)
java -cp bin client.ClientMain client1

# Interactive mode (type your own requests)
java -cp bin client.ClientMain client1 interactive
```

## Testing Failure Scenarios

### 1. Kill Primary Server (Test Failover)

**Windows:**

```batch
scripts\kill-primary.bat
```

**Linux/Mac:**

```bash
./scripts/kill-primary.sh
```

**Expected Behavior:**

- Monitor detects primary failure within 10 seconds
- Monitor elects Server 2 (lowest ID backup) as new primary
- Client automatically reconnects to new primary
- Check `logs/monitor.log` for failover details

### 2. Simulate Heartbeat Delay

**Linux/Mac:**

```bash
./scripts/delay-heartbeat.sh 1 15
# Suspends Server 1 for 15 seconds
```

**Windows:**

- Manually minimize Server 1 window and wait 15 seconds
- Or use Task Manager to suspend the process

**Expected Behavior:**

- If delay exceeds 10 seconds, failover triggers
- If delay is less, server recovers without failover

### 3. Multiple Failures

```bash
# Kill primary
./scripts/kill-primary.sh

# Wait for failover, then kill the new primary
# Check logs/monitor.log to see which server is now primary
# Kill that server to test cascading failover
```

## Ports Used

| Component          | Port |
| ------------------ | ---- |
| Monitor            | 5000 |
| Server 1 (Primary) | 6001 |
| Server 2 (Backup)  | 6002 |
| Server 3 (Backup)  | 6003 |

**Note:** Ensure these ports are not in use by other applications.

## Configuration

Key parameters can be adjusted in the source code:

**Monitor.java:**

- `HEARTBEAT_TIMEOUT_MS`: Failure detection timeout (default: 10000ms)
- `HEARTBEAT_CHECK_INTERVAL_MS`: How often to check (default: 2000ms)

**HeartbeatSender.java:**

- `HEARTBEAT_INTERVAL_MS`: Heartbeat frequency (default: 3000ms)

**Client.java:**

- `MAX_RETRY_ATTEMPTS`: Connection retry attempts (default: 3)
- `RETRY_DELAY_MS`: Delay between retries (default: 1000ms)

## Viewing Logs

Monitor all system activity:

```bash
# Monitor logs
tail -f logs/monitor.log

# Server logs
tail -f logs/server1.log
tail -f logs/server2.log
tail -f logs/server3.log
```

On Windows, use `type logs\monitor.log` to view logs.

## Stopping the System

**Windows:**

- Close all command windows
- Or use Task Manager to end Java processes

**Linux/Mac:**

```bash
# Find Java processes
ps aux | grep java

# Kill all SRMS processes
killall java

# Or kill specific PIDs from logs/*.pid files
```

## Troubleshooting

### Port Already in Use

```
Error: Address already in use
```

**Solution:** Change port numbers in source code or kill processes using those ports.

### Connection Refused

```
Error: Connection refused
```

**Solution:** Ensure monitor is running before starting servers/clients.

### ClassNotFoundException

```
Error: java.lang.ClassNotFoundException
```

**Solution:** Recompile with `javac -d bin -sourcepath src src/**/*.java`

## Project Requirements Met

✅ Multiple server processes (3 instances)  
✅ Primary-Backup architecture  
✅ Heartbeat mechanism (3s intervals)  
✅ Automatic failover (lowest ID election)  
✅ Client discovery via monitor  
✅ Timestamped logging  
✅ Failure simulation scripts  
✅ Clean OOP design (inheritance, encapsulation)

## Future Improvements

- State replication between primary and backups
- Split-brain prevention with quorum
- Dynamic server registration
- GUI admin interface
- Persistent state storage

## Team Members

[Add your team member information here]

## License

This project is for educational purposes as part of COMP 370 coursework.
