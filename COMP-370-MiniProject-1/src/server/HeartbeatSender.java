package server;

import common.Message;
import common.MessageType;
import common.ServerInfo;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Sends periodic heartbeats to the monitor. */
public class HeartbeatSender implements Runnable {
    private static final String MONITOR_HOST = "localhost";
    private static final int MONITOR_PORT = 5000;
    private static final long HEARTBEAT_INTERVAL_MS = 3000; // 3 seconds

    private ServerInfo serverInfo;
    private volatile boolean running;

    public HeartbeatSender(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        this.running = true;
    }

    @Override
    public void run() {
        log("Heartbeat sender started");

        while (running) {
            try {
                sendHeartbeat();
                Thread.sleep(HEARTBEAT_INTERVAL_MS);
            } catch (InterruptedException e) {
                log("Heartbeat sender interrupted");
                break;
            } catch (Exception e) {
                log("Error sending heartbeat: " + e.getMessage());
                // Continue trying even if one heartbeat fails
            }
        }

        log("Heartbeat sender stopped");
    }

    private void sendHeartbeat() throws IOException {
        try (Socket socket = new Socket(MONITOR_HOST, MONITOR_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Message heartbeatMsg = new Message(MessageType.HEARTBEAT, serverInfo);
            out.writeObject(heartbeatMsg);
            out.flush();

            try {
                Message ack = (Message) in.readObject();
                if (ack.getType() == MessageType.RESPONSE) {
                    log("Heartbeat acknowledged");
                }
            } catch (ClassNotFoundException e) {
                log("Invalid heartbeat acknowledgment: " + e.getMessage());
            }

            log("Heartbeat sent successfully");

        } catch (IOException e) {
            log("Failed to send heartbeat: " + e.getMessage());
            throw e;
        }
    }

    public void stop() {
        running = false;
    }

    public void setPrimary(boolean isPrimary) {
        if (serverInfo != null) {
            serverInfo.setPrimary(isPrimary);
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        System.out.println("[" + sdf.format(new Date()) + "] [HEARTBEAT-" +
                serverInfo.getServerId() + "] " + message);
    }
}
