package monitor;

import common.Message;
import common.MessageType;
import common.ServerInfo;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Monitor tracks heartbeats and handles failover. */
public class Monitor {
    private static final int MONITOR_PORT = 5000;
    private static final long HEARTBEAT_TIMEOUT_MS = 10000; // 10 seconds
    private static final long HEARTBEAT_CHECK_INTERVAL_MS = 2000; // 2 seconds

    private ServerSocket serverSocket;
    private Map<Integer, HeartbeatRecord> serverHeartbeats;
    private ServerInfo currentPrimary;
    private boolean running;

    public Monitor() {
        this.serverHeartbeats = new ConcurrentHashMap<>();
        this.running = false;
        this.currentPrimary = null;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(MONITOR_PORT);
        running = true;

        log("Monitor started on port " + MONITOR_PORT);

        Thread heartbeatChecker = new Thread(this::checkHeartbeats);
        heartbeatChecker.setDaemon(true);
        heartbeatChecker.start();

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread handler = new Thread(() -> handleConnection(clientSocket));
                handler.start();
            } catch (IOException e) {
                if (running) {
                    log("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    private void handleConnection(Socket socket) {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Message message = (Message) in.readObject();

            switch (message.getType()) {
                case HEARTBEAT:
                    handleHeartbeat(message, out);
                    break;
                case DISCOVER:
                    handleDiscovery(message, out);
                    break;
                default:
                    log("Unknown message type: " + message.getType());
            }

        } catch (Exception e) {
            log("Error handling connection: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private void handleHeartbeat(Message message, ObjectOutputStream out) {
        try {
            ServerInfo serverInfo = (ServerInfo) message.getPayload();
            int serverId = serverInfo.getServerId();

            log("Received heartbeat from Server-" + serverId +
                    " (" + (serverInfo.isPrimary() ? "PRIMARY" : "BACKUP") + ")");

            HeartbeatRecord record = serverHeartbeats.get(serverId);
            if (record == null) {
                record = new HeartbeatRecord(serverInfo);
                serverHeartbeats.put(serverId, record);
                log("Registered new server: " + serverInfo);

                if (serverInfo.isPrimary() && currentPrimary == null) {
                    currentPrimary = serverInfo;
                    log("Primary server registered: Server-" + serverId);
                }
            } else {
                record.updateHeartbeat();
            }

            Message ack = new Message(MessageType.RESPONSE, "ACK");
            out.writeObject(ack);
            out.flush();

        } catch (Exception e) {
            log("Error handling heartbeat: " + e.getMessage());
        }
    }

    private void handleDiscovery(Message message, ObjectOutputStream out) {
        log("Received discovery request from client");

        try {
            if (currentPrimary != null) {
                Message response = new Message(MessageType.PRIMARY_INFO, currentPrimary);
                out.writeObject(response);
                out.flush();
                log("Sent primary info: Server-" + currentPrimary.getServerId() +
                        " at " + currentPrimary.getHost() + ":" + currentPrimary.getPort());
            } else {
                log("WARNING: No primary server available to report to client");
                Message response = new Message(MessageType.PRIMARY_INFO, null);
                out.writeObject(response);
                out.flush();
            }
        } catch (IOException e) {
            log("Error sending discovery response: " + e.getMessage());
        }
    }

    private void checkHeartbeats() {
        while (running) {
            try {
                Thread.sleep(HEARTBEAT_CHECK_INTERVAL_MS);

                detectFailures();

            } catch (InterruptedException e) {
                log("Heartbeat checker interrupted");
                break;
            }
        }
    }

    private void detectFailures() {
        for (Map.Entry<Integer, HeartbeatRecord> entry : serverHeartbeats.entrySet()) {
            HeartbeatRecord record = entry.getValue();
            if (!record.isAlive(HEARTBEAT_TIMEOUT_MS)) {
                handleServerFailure(record.getServerInfo());
            }
        }
    }

    private void handleServerFailure(ServerInfo failedServer) {
        log("Server failure detected: " + failedServer);

        serverHeartbeats.remove(failedServer.getServerId());

        if (currentPrimary != null && currentPrimary.getServerId() == failedServer.getServerId()) {
            log("Primary server failed! Initiating failover...");
            electNewPrimary();
        }
    }

    private void electNewPrimary() {
        log("Electing new primary...");

        ServerInfo newPrimary = selectNewPrimary();

        if (newPrimary != null) {
            promoteBackupToPrimary(newPrimary);
        } else {
            log("ERROR: No backup servers available for promotion!");
        }
    }

    private ServerInfo selectNewPrimary() {
        ServerInfo selected = null;
        int lowestId = Integer.MAX_VALUE;

        for (HeartbeatRecord record : serverHeartbeats.values()) {
            if (record.isAlive(HEARTBEAT_TIMEOUT_MS)) {
                ServerInfo serverInfo = record.getServerInfo();
                int serverId = serverInfo.getServerId();

                if (serverId < lowestId) {
                    lowestId = serverId;
                    selected = serverInfo;
                }
            }
        }

        if (selected != null) {
            log("Selected Server-" + selected.getServerId() + " as new primary (lowest ID)");
        }

        return selected;
    }

    private void promoteBackupToPrimary(ServerInfo newPrimary) {
        log("Promoting Server-" + newPrimary.getServerId() + " to primary");

        try (Socket socket = new Socket(newPrimary.getHost(), newPrimary.getPort());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Message promoteMsg = new Message(MessageType.PROMOTE, newPrimary.getServerId());
            out.writeObject(promoteMsg);
            out.flush();

            Message ack = (Message) in.readObject();

            if (ack.getType() == MessageType.RESPONSE) {
                newPrimary.setPrimary(true);
                currentPrimary = newPrimary;

                log("*** FAILOVER COMPLETE: Server-" + newPrimary.getServerId() +
                        " is now PRIMARY ***");
            }

        } catch (Exception e) {
            log("ERROR: Failed to promote server: " + e.getMessage());
            // If promotion fails, try to elect another backup
            currentPrimary = null;
        }
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Error shutting down: " + e.getMessage());
        }
        log("Monitor shutdown complete");
    }

    private void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        System.out.println("[" + sdf.format(new Date()) + "] [MONITOR] " + message);
    }
}
