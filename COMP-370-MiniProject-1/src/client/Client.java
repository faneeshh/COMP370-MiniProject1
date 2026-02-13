package client;

import common.Message;
import common.MessageType;
import common.ServerInfo;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Client for discovery and request sending. */
public class Client {
    private static final String MONITOR_HOST = "localhost";
    private static final int MONITOR_PORT = 5000;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private String clientId;
    private ServerInfo currentPrimaryInfo;

    public Client(String clientId) {
        this.clientId = clientId;
        this.currentPrimaryInfo = null;
    }

    public boolean initialize() {
        log("Initializing client...");

        currentPrimaryInfo = discoverPrimary();

        if (currentPrimaryInfo != null) {
            log("Successfully discovered primary: " + currentPrimaryInfo);
            return true;
        } else {
            log("Failed to discover primary server");
            return false;
        }
    }

    private ServerInfo discoverPrimary() {
        log("Querying monitor for primary server...");

        try (Socket monitorSocket = new Socket(MONITOR_HOST, MONITOR_PORT);
                ObjectOutputStream out = new ObjectOutputStream(monitorSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(monitorSocket.getInputStream())) {

            Message discoverMsg = new Message(MessageType.DISCOVER, null);
            out.writeObject(discoverMsg);
            out.flush();

            Message response = (Message) in.readObject();

            if (response.getType() == MessageType.PRIMARY_INFO) {
                ServerInfo primaryInfo = (ServerInfo) response.getPayload();
                if (primaryInfo != null) {
                    log("Discovered primary: Server-" + primaryInfo.getServerId() +
                            " at " + primaryInfo.getHost() + ":" + primaryInfo.getPort());
                    return primaryInfo;
                } else {
                    log("Monitor reports no primary available");
                    return null;
                }
            }

        } catch (Exception e) {
            log("Error discovering primary: " + e.getMessage());
        }

        return null;
    }

    public String sendRequest(String requestData) {
        log("Sending request: " + requestData);

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                String response = sendRequestToPrimary(requestData);
                if (response != null) {
                    return response;
                }
            } catch (IOException e) {
                log("Request failed (attempt " + attempt + "): " + e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    log("Attempting to rediscover primary...");
                    currentPrimaryInfo = discoverPrimary();

                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log("Request failed after " + MAX_RETRY_ATTEMPTS + " attempts");
        return null;
    }

    private String sendRequestToPrimary(String requestData) throws IOException {
        if (currentPrimaryInfo == null) {
            throw new IOException("No primary server information available");
        }

        try (Socket socket = new Socket(currentPrimaryInfo.getHost(),
                currentPrimaryInfo.getPort());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            Message requestMsg = new Message(MessageType.PROCESS, requestData);
            out.writeObject(requestMsg);
            out.flush();

            Message responseMsg = (Message) in.readObject();

            if (responseMsg.getType() == MessageType.RESPONSE) {
                String response = (String) responseMsg.getPayload();
                log("Received response: " + response);
                return response;
            } else {
                log("Unexpected response type: " + responseMsg.getType());
                return null;
            }

        } catch (ClassNotFoundException e) {
            log("Error deserializing response: " + e.getMessage());
            throw new IOException("Failed to deserialize response", e);
        }
    }

    public void shutdown() {
        log("Client shutting down");
    }

    private void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        System.out.println("[" + sdf.format(new Date()) + "] [CLIENT-" +
                clientId + "] " + message);
    }
}
