# SRMS Test Scenarios and Results

## Test Environment

- **Operating System:** [Your OS]
- **Java Version:** [Your Java version]
- **Date:** [Test date]
- **Testers:** [Team member names]

---

## Test Scenario 1: Normal Operation

### Description

Start monitor and 3 servers, verify primary election, send client requests.

### Steps

1. Start monitor: `./scripts/run.sh` (or `run.bat`)
2. Wait 3 seconds for all servers to register
3. Start client: `java -cp bin client.ClientMain client1`
4. Send 5 test requests

### Expected Results

- Monitor starts on port 5000
- Server 1 starts as PRIMARY on port 6001
- Servers 2 and 3 start as BACKUP on ports 6002, 6003
- All servers send heartbeats every 3 seconds
- Client discovers primary successfully
- All requests processed and responses received

### Actual Results

```
[Record timestamps and logs here]

Example:
[2026-02-12 10:15:23.456] [MONITOR] Monitor started on port 5000
[2026-02-12 10:15:25.789] [SERVER-1-PRIMARY] Server started on port 6001
[2026-02-12 10:15:26.123] [SERVER-2-BACKUP] Server started on port 6002
[2026-02-12 10:15:26.456] [SERVER-3-BACKUP] Server started on port 6003
[2026-02-12 10:15:30.001] [CLIENT-client1] Successfully discovered primary: Server-1
[2026-02-12 10:15:30.234] [CLIENT-client1] Received response: PROCESSED[1]: Process data batch 1
```

### Pass/Fail: ⬜ PASS ⬜ FAIL

---

## Test Scenario 2: Primary Server Crash

### Description

Kill the primary server and verify automatic failover to backup.

### Steps

1. Start system (all components running normally)
2. Verify client can send requests to Server 1
3. Execute: `./scripts/kill-primary.sh`
4. Monitor logs for failover detection
5. Send new client request

### Expected Results

- Monitor detects Server 1 failure within 10 seconds
- Monitor elects Server 2 (lowest ID backup) as new primary
- Monitor sends PROMOTE message to Server 2
- Server 2 transitions to PRIMARY role
- Client rediscovers primary (Server 2)
- Client requests succeed with new primary

### Actual Results

```
[Record timestamps and logs here]

Key events to capture:
- Time of server kill: [timestamp]
- Time monitor detected failure: [timestamp]
- Detection time: [calculate difference]
- Time Server 2 promoted: [timestamp]
- Failover time: [calculate difference]
- Time client reconnected: [timestamp]
- First successful request after failover: [timestamp]
```

### State Consistency

⬜ No data loss observed  
⬜ All requests after failover processed correctly  
⬜ Backup servers synchronized with new primary

### Pass/Fail: ⬜ PASS ⬜ FAIL

---

## Test Scenario 3: Backup Server Crash

### Description

Kill a backup server and verify system continues operating normally.

### Steps

1. Start system normally
2. Identify Server 3 PID from logs/server3.pid
3. Kill Server 3: `kill -9 <PID>` (Linux) or Task Manager (Windows)
4. Send client requests to primary
5. Restart Server 3

### Expected Results

- Primary (Server 1) continues processing requests
- Server 3 stops sending heartbeats
- Monitor logs Server 3 failure
- No failover triggered (primary still alive)
- When Server 3 restarts, it re-registers with monitor
- Server 3 resumes BACKUP role

### Actual Results

```
[Record observations here]
```

### Pass/Fail: ⬜ PASS ⬜ FAIL

---

## Test Scenario 4: Simultaneous Failures

### Description

Kill primary and one backup simultaneously, verify system still elects new primary.

### Steps

1. Start system normally
2. Record PIDs for Server 1 and Server 2
3. Kill both: `kill -9 <PID1> <PID2>`
4. Monitor should elect Server 3 as primary
5. Send client requests

### Expected Results

- Monitor detects both failures within 10 seconds
- Monitor elects Server 3 (only remaining server) as primary
- Client successfully connects to Server 3
- System continues operating with single server

### Actual Results

```
[Record observations]
```

### Pass/Fail: ⬜ PASS ⬜ FAIL

---

## Test Scenario 5: Network Delay Simulation

### Description

Suspend server process to simulate heartbeat delays.

### Steps

1. Start system normally
2. Run: `./scripts/delay-heartbeat.sh 1 15`
3. This suspends Server 1 for 15 seconds
4. Monitor should detect failure (15s > 10s timeout)
5. Resume happens automatically after 15s

### Expected Results

- Server 1 heartbeats stop
- After 10 seconds, monitor triggers failover
- Server 2 becomes primary
- After 15 seconds, Server 1 resumes
- Server 1 re-registers as backup

### Actual Results

```
[Record observations]

Note: If delay < 10s, no failover should occur
```

### Pass/Fail: ⬜ PASS ⬜ FAIL

---

## Test Scenario 6: Recovery and Rejoin

### Description

Restart all crashed servers and verify they rejoin the cluster.

### Steps

1. Kill all servers (primary and backups)
2. Wait 15 seconds
3. Restart servers manually:
   - `java -cp bin server.PrimaryServer 1 localhost 6001`
   - `java -cp bin server.BackupServer 2 localhost 6002`
   - `java -cp bin server.BackupServer 3 localhost 6003`
4. Verify heartbeats resume
5. Verify client can connect

### Expected Results

- All servers restart successfully
- Servers send heartbeats to monitor
- Monitor re-registers all servers
- Server 1 becomes primary (or lowest ID available)
- Client discovers primary and sends requests

### Actual Results

```
[Record observations]
```

### Pass/Fail: ⬜ PASS ⬜ FAIL

---

## Test Scenario 7: Cascading Failover

### Description

Test multiple consecutive failovers.

### Steps

1. Start with Server 1 as primary
2. Kill Server 1 → Server 2 becomes primary
3. Kill Server 2 → Server 3 becomes primary
4. Verify client follows all failovers

### Expected Results

- Each failover completes within 12 seconds
- Client successfully reconnects after each failover
- Final primary is Server 3

### Actual Results

```
[Record all three failover events with timestamps]

Failover 1 (1→2): [times]
Failover 2 (2→3): [times]
Client final connection: [details]
```

### Pass/Fail: ⬜ PASS ⬜ FAIL

---

## Summary of Test Results

| Scenario              | Status | Detection Time | Failover Time | Notes |
| --------------------- | ------ | -------------- | ------------- | ----- |
| Normal Operation      |        | N/A            | N/A           |       |
| Primary Crash         |        |                |               |       |
| Backup Crash          |        | N/A            | N/A           |       |
| Simultaneous Failures |        |                |               |       |
| Network Delay         |        |                |               |       |
| Recovery              |        | N/A            | N/A           |       |
| Cascading Failover    |        |                |               |       |

---

## Performance Metrics

### Heartbeat Timing

- **Heartbeat Interval:** 3 seconds
- **Failure Detection Timeout:** 10 seconds
- **Measured Detection Time:** [avg of all tests]
- **Measured Failover Time:** [avg of all tests]

### Requirements Compliance

- ✅ NFR1: Failure detected within 5 heartbeats (15s max) → [measured: ___ s]
- ✅ NFR2: Resume serving within 2 seconds after failover → [measured: ___ s]

---

## Issues Encountered

### Issue 1: [Description]

- **Severity:** High/Medium/Low
- **Impact:**
- **Resolution:**
- **Status:** Fixed/Workaround/Known Issue

### Issue 2: [Description]

...

---

## Lessons Learned

1. [Lesson learned from testing]
2. [What worked well]
3. [What could be improved]
4. [Unexpected behaviors observed]

---

## Recommendations for Future Improvements

1. **State Replication:** Implement state sync between primary and backups
2. **Split-Brain Prevention:** Add quorum requirements
3. **Dynamic Configuration:** Allow servers to join/leave without restart
4. **Persistent Logs:** Store logs to database for long-term analysis
5. **Health Metrics:** Add CPU, memory, request latency monitoring

---

## Test Team Signatures

- **Tester 1:** ********\_******** Date: **\_\_\_**
- **Tester 2:** ********\_******** Date: **\_\_\_**
- **Tester 3:** ********\_******** Date: **\_\_\_**

---

**End of Test Documentation**
