package server;

import common.Message;
import common.ServerInfo;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Base class for server processes. */
public abstract class ServerProcess {
    protected int serverId;
    protected String host;
    protected int port;
    protected boolean isPrimary;
    protected ServerSocket serverSocket;
    protected volatile boolean running;
    protected Thread heartbeatThread;
    protected HeartbeatSender heartbeatSender;

    public ServerProcess(int serverId, String host, int port, boolean isPrimary) {
        this.serverId = serverId;
        this.host = host;
        this.port = port;
        this.isPrimary = isPrimary;
        this.running = false;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        log("Server started on port " + port + " as " +
                (isPrimary ? "PRIMARY" : "BACKUP"));

        ServerInfo serverInfo = new ServerInfo(serverId, host, port, isPrimary);
        heartbeatSender = new HeartbeatSender(serverInfo);
        heartbeatThread = new Thread(heartbeatSender);
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread handler = new Thread(() -> handleClient(clientSocket));
                handler.start();
            } catch (IOException e) {
                if (running) {
                    log("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    protected abstract void handleClient(Socket clientSocket);

    public void promoteToprimary() {
        synchronized (this) {
            if (!isPrimary) {
                isPrimary = true;
                if (heartbeatSender != null) {
                    heartbeatSender.setPrimary(true);
                }
                log("*** PROMOTED TO PRIMARY ***");

                onPromotedToPrimary();
            }
        }
    }

    protected void onPromotedToPrimary() {
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public int getServerId() {
        return serverId;
    }

    public ServerInfo getServerInfo() {
        return new ServerInfo(serverId, host, port, isPrimary);
    }

    public void shutdown() {
        running = false;

        if (heartbeatSender != null) {
            heartbeatSender.stop();
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Error closing server socket: " + e.getMessage());
        }

        log("Server shutdown complete");
    }

    protected void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String role = isPrimary ? "PRIMARY" : "BACKUP";
        System.out.println("[" + sdf.format(new Date()) + "] [SERVER-" +
                serverId + "-" + role + "] " + message);
    }
}
