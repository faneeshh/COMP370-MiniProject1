# COMP 370 – Mini Project 1: Server Redundancy Management System

This repository contains our implementation of the Server Redundancy Management System (SRMS) for COMP 370.  
The system simulates a primary–backup server cluster with a monitor and client, including heartbeats, failover, and testing scenarios.

## Project Structure

- **src/monitor** – Monitor logic (heartbeats, failure detection, primary election)
- **src/server** – ServerProcess base class, PrimaryServer, BackupServer, heartbeat sender, promotion handling
- **src/client** – Client that discovers the primary via the monitor and sends requests
- **src/common** – Shared message formats, utilities, and constants
- **scripts/** – Run scripts and failure simulation scripts (kill primary, delay heartbeat, etc.)
- **docs/** – UML diagrams (use case, class, sequence) and design notes
- **tests/** – Test scenario descriptions, timestamps, and results
- **.gitignore** – Ignore build artifacts and IDE files

## How to Run (to be completed later)

- Start the monitor
- Start 3 server instances (1 primary, 2 backups)
- Start the client
- Use scripts in `scripts/` to simulate failures

This section will be updated as we implement the system.

## Team Task Breakdown

Each teammate will focus on one main area:

1. Monitor + heartbeat tracking  
2. Server implementation (primary + backup)  
3. Client implementation  
4. UML diagrams + report writing  
5. Testing + failure scenarios

Integration and overall structure will be coordinated through this repository.
