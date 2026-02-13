# SRMS Quick Reference Guide

## Common Commands

### Start the System

```bash
# Linux/Mac
./scripts/run.sh

# Windows
scripts\run.bat
```

### Stop the System

```bash
# Linux/Mac
./scripts/stop-all.sh

# Windows
scripts\stop-all.bat
```

### Run a Test Client

```bash
# Automated mode (5 test requests)
java -cp bin client.ClientMain client1

# Interactive mode (type your own requests)
java -cp bin client.ClientMain client1 interactive

# Using script
./scripts/test-client.sh client1
```

### Simulate Failures

#### Kill Primary Server

```bash
# Linux/Mac
./scripts/kill-primary.sh

# Windows
scripts\kill-primary.bat
```

#### Delay Heartbeat (Simulate Network Issues)

```bash
# Linux/Mac - suspend Server 1 for 15 seconds
./scripts/delay-heartbeat.sh 1 15

# Windows - manually minimize server window for 15 seconds
```

### View Logs in Real-Time

```bash
# Linux/Mac
tail -f logs/monitor.log
tail -f logs/server1.log
tail -f logs/server2.log
tail -f logs/server3.log

# Windows
powershell Get-Content logs\monitor.log -Wait
```

### Manual Server Start (if needed)

```bash
# Monitor
java -cp bin monitor.MonitorMain

# Primary Server
java -cp bin server.PrimaryServer 1 localhost 6001

# Backup Servers
java -cp bin server.BackupServer 2 localhost 6002
java -cp bin server.BackupServer 3 localhost 6003
```

### Compile Only

```bash
# Linux/Mac
javac -d bin -sourcepath src src/**/*.java

# Windows
javac -d bin -sourcepath src src\common\*.java src\monitor\*.java src\server\*.java src\client\*.java
```

### Clean Build

```bash
# Linux/Mac
rm -rf bin logs
mkdir -p bin logs

# Windows
rmdir /s /q bin logs
mkdir bin logs
```

## Port Reference

| Component | Port | Purpose                              |
| --------- | ---- | ------------------------------------ |
| Monitor   | 5000 | Heartbeat tracking, client discovery |
| Server 1  | 6001 | Primary server (client requests)     |
| Server 2  | 6002 | Backup server                        |
| Server 3  | 6003 | Backup server                        |

## Configuration Parameters

### Monitor (Monitor.java)

- `HEARTBEAT_TIMEOUT_MS = 10000` - Time before server considered failed
- `HEARTBEAT_CHECK_INTERVAL_MS = 2000` - How often to check heartbeats
- `MONITOR_PORT = 5000` - Port monitor listens on

### HeartbeatSender (HeartbeatSender.java)

- `HEARTBEAT_INTERVAL_MS = 3000` - How often to send heartbeats
- `MONITOR_HOST = "localhost"` - Monitor hostname
- `MONITOR_PORT = 5000` - Monitor port

### Client (Client.java)

- `MAX_RETRY_ATTEMPTS = 3` - Connection retry attempts
- `RETRY_DELAY_MS = 1000` - Delay between retries
- `MONITOR_HOST = "localhost"` - Monitor hostname
- `MONITOR_PORT = 5000` - Monitor port

## Message Types

| Type         | From    | To      | Purpose                   |
| ------------ | ------- | ------- | ------------------------- |
| HEARTBEAT    | Server  | Monitor | Server health signal      |
| DISCOVER     | Client  | Monitor | Request primary info      |
| PRIMARY_INFO | Monitor | Client  | Return current primary    |
| PROCESS      | Client  | Server  | Request processing        |
| RESPONSE     | Server  | Client  | Response to request       |
| PROMOTE      | Monitor | Server  | Promote backup to primary |

## Troubleshooting

### "Port already in use"

```bash
# Find process using port 5000
lsof -i :5000  # Mac/Linux
netstat -ano | findstr :5000  # Windows

# Kill the process
kill <PID>  # Mac/Linux
taskkill /PID <PID> /F  # Windows
```

### "Connection refused"

- Ensure monitor is running before starting servers/clients
- Check firewall settings
- Verify correct ports in code

### "ClassNotFoundException"

- Recompile: `javac -d bin -sourcepath src src/**/*.java`
- Check CLASSPATH: `java -cp bin <ClassName>`

### Server won't start

- Check if port is available
- Look at logs for error messages
- Ensure Java is installed: `java -version`

### Failover not triggering

- Verify HEARTBEAT_TIMEOUT_MS > HEARTBEAT_INTERVAL_MS
- Check monitor logs for heartbeat reception
- Ensure server was actually killed (check process list)

## Testing Checklist

- [ ] System starts successfully (all 4 processes)
- [ ] All servers send heartbeats every 3 seconds
- [ ] Client can discover primary
- [ ] Client can send requests and receive responses
- [ ] Kill primary → failover within 12 seconds
- [ ] Client reconnects after failover
- [ ] Kill backup → no impact on primary
- [ ] Restart killed server → rejoins as backup
- [ ] Multiple failovers work correctly
- [ ] All events logged with timestamps

## File Structure

```
COMP-370-MiniProject-1/
├── src/
│   ├── common/      - Message, ServerInfo, MessageType
│   ├── monitor/     - Monitor, MonitorMain, HeartbeatRecord
│   ├── server/      - ServerProcess, Primary, Backup, HeartbeatSender
│   └── client/      - Client, ClientMain
├── scripts/
│   ├── run.sh/.bat              - Start system
│   ├── stop-all.sh/.bat         - Stop system
│   ├── kill-primary.sh/.bat     - Kill primary
│   ├── delay-heartbeat.sh/.bat  - Simulate delay
│   └── test-client.sh/.bat      - Run test client
├── bin/             - Compiled .class files
├── logs/            - Runtime logs
├── README.md        - Full documentation
├── TEST-SCENARIOS.md - Test documentation
└── REPORT-TEMPLATE.md - Report template
```

## Quick Test Sequence

1. **Start system:** `./scripts/run.sh`
2. **Wait 5 seconds** for initialization
3. **Run client:** `./scripts/test-client.sh client1`  
   Expected: 5 requests processed successfully
4. **Kill primary:** `./scripts/kill-primary.sh`  
   Expected: Failover within 12 seconds
5. **Run client again:** `./scripts/test-client.sh client2`  
   Expected: Requests processed by new primary (Server 2)
6. **Check logs:** `tail -f logs/monitor.log`  
   Expected: See failover sequence logged
7. **Stop system:** `./scripts/stop-all.sh`

## Getting Help

- Check README.md for detailed instructions
- Review TEST-SCENARIOS.md for test procedures
- Look at logs/ directory for error messages
- Refer to inline code comments for logic details
- Check COMP 370 lecture notes for concepts

## Academic Integrity Note

This is coursework. Understand every line of code. Be able to explain:

- How heartbeat mechanism works
- How failover election works
- How client discovery works
- OOP principles used
- Concurrency handling
