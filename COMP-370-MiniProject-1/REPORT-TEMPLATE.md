# COMP 370 Mini Project 1 - Project Report Template

## Server Redundancy Management System (SRMS)

---

## Team Information

| Name         | Student ID | Email          | Role/Responsibility      |
| ------------ | ---------- | -------------- | ------------------------ |
| [First Last] | [ID]       | [email@ufv.ca] | Monitor & Failover Logic |
| [First Last] | [ID]       | [email@ufv.ca] | Server Implementation    |
| [First Last] | [ID]       | [email@ufv.ca] | Client & Testing         |
| [First Last] | [ID]       | [email@ufv.ca] | UML & Documentation      |
| [First Last] | [ID]       | [email@ufv.ca] | Integration & Scripts    |

---

## 1. Project Overview and Objectives

### 1.1 Problem Statement

[Write 2-3 sentences describing the problem your SRMS solves]

Example:

> Distributed systems require high availability to serve clients reliably. Our Server Redundancy Management System (SRMS) implements a primary-backup architecture with automatic failover to ensure continuous service even when servers fail. The system uses heartbeat monitoring and deterministic election to maintain service availability with minimal downtime.

### 1.2 Project Objectives

- Implement a fault-tolerant distributed server system
- Demonstrate automatic failover capabilities
- Provide transparent failure recovery to clients
- Apply software engineering principles (OOP, UML, testing)

---

## 2. Requirements Engineering

### 2.1 Stakeholders

- **System Administrator:** Manages server deployment and monitors health
- **End Users/Clients:** Send requests expecting reliable responses
- **Development Team:** Implements and maintains the system

### 2.2 Functional Requirements

| ID  | Requirement                                                                               | Priority | Status         |
| --- | ----------------------------------------------------------------------------------------- | -------- | -------------- |
| FR1 | System shall allow clients to send requests and receive responses from the primary server | High     | ✅ Implemented |
| FR2 | System shall automatically elect a backup to become primary if current primary fails      | High     | ✅ Implemented |
| FR3 | Monitor shall track server health via periodic heartbeat messages                         | High     | ✅ Implemented |
| FR4 | System shall use lowest server ID for deterministic primary election                      | Medium   | ✅ Implemented |
| FR5 | Client shall automatically discover and reconnect to new primary after failover           | High     | ✅ Implemented |
| FR6 | System shall log all events with timestamps                                               | Medium   | ✅ Implemented |

### 2.3 Non-Functional Requirements

| ID   | Requirement                                                        | Target | Measured |
| ---- | ------------------------------------------------------------------ | ------ | -------- |
| NFR1 | System shall detect primary failure within 5 heartbeats (15s max)  | ≤ 15s  | [X.X]s   |
| NFR2 | System shall resume serving requests within 2s after failover      | ≤ 2s   | [X.X]s   |
| NFR3 | Heartbeat interval shall be 3 seconds                              | 3s     | 3s       |
| NFR4 | System shall support minimum 3 server instances                    | ≥ 3    | 3        |
| NFR5 | Code shall follow OOP principles (encapsulation, inheritance, SRP) | -      | ✅ Met   |

---

## 3. System Design and Architecture

### 3.1 High-Level Architecture

[Insert architecture diagram or describe]

Our SRMS consists of four main components:

1. **Monitor Process:** Central coordinator running on port 5000
2. **Server Processes:** 1 Primary + N Backups (ports 6001-600N)
3. **Client Process:** Discovers primary and sends requests
4. **Communication:** Socket-based message passing with Java serialization

### 3.2 UML Diagrams

#### 3.2.1 Use Case Diagram

[Insert your use case diagram image here]

Key Use Cases:

- **UC1:** Client sends request to primary
- **UC2:** Server sends heartbeat to monitor
- **UC3:** Monitor detects failure and promotes backup
- **UC4:** Client discovers primary server

#### 3.2.2 Class Diagram

[Insert your class diagram image here]

Key Classes:

- **ServerProcess (abstract):** Base class for all servers
- **PrimaryServer:** Handles client requests
- **BackupServer:** Standby mode, waits for promotion
- **Monitor:** Tracks heartbeats, manages failover
- **Client:** Discovers primary, sends requests
- **HeartbeatSender:** Background thread for heartbeats
- **Message, ServerInfo, MessageType:** Communication models

#### 3.2.3 Sequence Diagrams

**Diagram 1: Startup and Leader Election**
[Insert sequence diagram]

Shows:

1. Monitor starts and opens socket
2. Servers start and register with monitor
3. Primary is identified or elected
4. Heartbeats begin

**Diagram 2: Normal Client Request Flow**
[Insert sequence diagram]

Shows:

1. Client queries monitor for primary
2. Monitor returns primary ServerInfo
3. Client connects to primary
4. Client sends PROCESS request
5. Primary sends RESPONSE

**Diagram 3: Failover Scenario**
[Insert sequence diagram]

Shows:

1. Primary server fails (stops sending heartbeats)
2. Monitor detects timeout
3. Monitor selects lowest ID backup
4. Monitor sends PROMOTE message to backup
5. Backup transitions to primary
6. Client rediscovers new primary
7. Client resumes normal operation

---

## 4. Implementation Details

### 4.1 Design Principles Applied

#### Encapsulation

- Server internals hidden behind public API
- HeartbeatSender encapsulates heartbeat logic
- Monitor state protected with synchronized access

#### Single Responsibility Principle

- **ServerProcess:** Socket management and lifecycle
- **HeartbeatSender:** Only sends heartbeats
- **Monitor:** Only tracks health and manages failover
- **Client:** Only sends requests and handles reconnection

#### Inheritance

```
ServerProcess (abstract)
    ├── PrimaryServer
    └── BackupServer
```

Common functionality in base class, specific behavior in subclasses.

### 4.2 Networking and Concurrency

**ServerSocket:** Each server listens for connections  
**Thread Pool:** Each connection handled in separate thread  
**Heartbeat Thread:** Background daemon thread per server  
**Concurrent Data Structures:** ConcurrentHashMap for thread-safe heartbeat tracking

### 4.3 Failover Logic

**Election Algorithm:**

```java
1. Monitor detects primary failure (no heartbeat for 10s)
2. Monitor iterates all registered servers
3. Select alive server with lowest ID
4. Send PROMOTE message to selected server
5. Update currentPrimary reference
6. Selected server transitions to PRIMARY role
```

**Deterministic Election:** Using lowest ID ensures consistency and avoids split-brain scenarios where multiple servers believe they are primary.

---

## 5. Key Functions Implemented

### 5.1 Monitor.handleHeartbeat()

**Purpose:** Processes heartbeat messages from servers  
**How it works:**

1. Extracts ServerInfo from message payload
2. Creates or updates HeartbeatRecord
3. Registers new servers dynamically
4. Sends acknowledgment to server

### 5.2 Monitor.detectFailures()

**Purpose:** Identifies servers that have failed  
**How it works:**

1. Iterates all HeartbeatRecords
2. Checks if last heartbeat exceeds timeout (10s)
3. If server failed and is primary → triggers electNewPrimary()
4. Removes failed server from tracking map

### 5.3 Monitor.electNewPrimary()

**Purpose:** Selects and promotes new primary  
**How it works:**

1. Calls selectNewPrimary() to find lowest ID backup
2. Calls promoteBackupToPrimary() to send PROMOTE message
3. Updates currentPrimary reference
4. Logs failover completion

### 5.4 ServerProcess.promoteToprimary()

**Purpose:** Transitions backup to primary role  
**How it works:**

1. Sets isPrimary flag to true
2. Logs promotion event
3. Calls onPromotedToPrimary() hook for subclass-specific logic

### 5.5 Client.discoverPrimary()

**Purpose:** Queries monitor for current primary  
**How it works:**

1. Connects to monitor on port 5000
2. Sends DISCOVER message
3. Receives PRIMARY_INFO response with ServerInfo
4. Returns ServerInfo or null if no primary available

### 5.6 Client.sendRequest()

**Purpose:** Sends request with retry and failover handling  
**How it works:**

1. Attempts to send to currentPrimary
2. If fails, rediscovers primary from monitor
3. Retries up to 3 times with exponential backoff
4. Returns response or null if all attempts fail

---

## 6. Testing and Failure Scenarios

### 6.1 Test Environment

- **OS:** [Your system]
- **Java Version:** [Version]
- **Date Tested:** [Date]

### 6.2 Test Results Summary

| Scenario              | Status  | Detection Time | Failover Time | Notes                  |
| --------------------- | ------- | -------------- | ------------- | ---------------------- |
| Normal Operation      | ✅ Pass | N/A            | N/A           | All requests processed |
| Primary Crash         | ✅ Pass | [X.X]s         | [X.X]s        | Server 2 elected       |
| Backup Crash          | ✅ Pass | N/A            | N/A           | No disruption          |
| Simultaneous Failures | ✅ Pass | [X.X]s         | [X.X]s        | Server 3 elected       |
| Network Delay (15s)   | ✅ Pass | [X.X]s         | [X.X]s        | Failover triggered     |
| Cascading Failover    | ✅ Pass | [X.X]s         | [X.X]s        | All 3 servers tested   |

[Include detailed results from TEST-SCENARIOS.md]

### 6.3 Performance Metrics

- **Average Failure Detection:** [X.X] seconds
- **Average Failover Completion:** [X.X] seconds
- **Total Recovery Time:** [X.X] seconds
- **Client Reconnection:** [X.X] seconds

✅ **NFR1 Compliance:** Detection within 15s → Measured: [X.X]s  
✅ **NFR2 Compliance:** Resume within 2s → Measured: [X.X]s

---

## 7. Challenges and Solutions

### Challenge 1: [e.g., Socket Connection Timing]

**Problem:** [Describe the challenge]  
**Impact:** [What broke or didn't work]  
**Solution:** [How you fixed it]  
**Lesson Learned:** [What you learned]

### Challenge 2: [e.g., Thread Synchronization]

**Problem:**  
**Impact:**  
**Solution:**  
**Lesson Learned:**

### Challenge 3: [e.g., Heartbeat Timing Tuning]

**Problem:**  
**Impact:**  
**Solution:**  
**Lesson Learned:**

---

## 8. Team Contributions

### [Team Member 1 Name]

- Implemented Monitor class with heartbeat tracking
- Designed and coded failover election algorithm
- Created monitor logging system
- Contribution: ~25%

### [Team Member 2 Name]

- Implemented ServerProcess, PrimaryServer, BackupServer
- Developed HeartbeatSender thread
- Handled server promotion logic
- Contribution: ~25%

### [Team Member 3 Name]

- Implemented Client discovery and request handling
- Developed retry and reconnection logic
- Wrote test client scenarios
- Contribution: ~20%

### [Team Member 4 Name]

- Created all UML diagrams (use case, class, sequence)
- Wrote project report and documentation
- Designed system architecture
- Contribution: ~15%

### [Team Member 5 Name]

- Wrote run scripts (Bash and Batch)
- Implemented failure simulation scripts
- Conducted all test scenarios
- Created test documentation
- Contribution: ~15%

---

## 9. Future Improvements

1. **State Replication:** Synchronize data between primary and backups for stateful services
2. **Quorum-Based Election:** Prevent split-brain with majority voting
3. **Dynamic Server Registration:** Allow servers to join/leave cluster dynamically
4. **Persistent State:** Store server states and logs in database
5. **GUI Admin Panel:** Web interface for monitoring and manual failover
6. **Load Balancing:** Distribute client requests across multiple servers
7. **Configurable Parameters:** External config file for ports, timeouts
8. **Health Metrics:** Add CPU, memory, network metrics to heartbeats
9. **TLS/SSL:** Encrypt communication between components
10. **Docker Deployment:** Containerize for easy deployment

---

## 10. Conclusion

[Write 3-4 sentences summarizing your project]

Example:

> Our SRMS successfully demonstrates automatic failover in a distributed server environment. The system detects failures within [X] seconds and completes failover within [Y] seconds, meeting all specified requirements. Through this project, we gained hands-on experience with socket programming, multi-threading, OOP design, and distributed systems concepts. The implementation is robust, well-documented, and ready for demonstration and evaluation.

---

## References

1. Java Socket Programming Documentation - Oracle
2. Distributed Systems: Principles and Paradigms - Tanenbaum & Van Steen
3. COMP 370 Lecture Notes - UFV
4. Git Repository: [Your GitHub repo URL]

---

**End of Report**

**Page Count:** [X] / 5 pages max
**Submission Date:** [Date]
**Team Name:** [Your team name]
