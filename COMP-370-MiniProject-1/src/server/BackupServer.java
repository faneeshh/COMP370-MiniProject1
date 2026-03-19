package server;

import common.Message;
import common.MessageType;
import java.io.*;
import java.net.Socket;

/** Backup server implementation. */
public class BackupServer extends ServerProcess {

    public BackupServer(int serverId, String host, int port) {
        super(serverId, host, port, false); // Starts as backup
    }

    @Override
    protected void handleClient(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            Message message = (Message) in.readObject();
            log("Received message: " + message);

            if (message.getType() == MessageType.PROMOTE) {
                log("Received PROMOTE message from monitor");
                promoteToprimary();

                Message ack = new Message(MessageType.RESPONSE, "PROMOTED");
                out.writeObject(ack);
                out.flush();

            } else if (message.getType() == MessageType.PROCESS) {
                if (isPrimary) {
                    String requestData = (String) message.getPayload();
                    String responseData = processRequest(requestData);

                    Message response = new Message(MessageType.RESPONSE, responseData);
                    out.writeObject(response);
                    out.flush();

                    log("Processed request as primary: " + responseData);
                } else {
                    log("Received request while still backup - rejecting");
                    Message response = new Message(MessageType.RESPONSE,
                            "ERROR: This is a backup server");
                    out.writeObject(response);
                    out.flush();
                }
            } else {
                log("Unknown message type: " + message.getType());
            }

        } catch (Exception e) {
            log("Error handling connection: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private String processRequest(String requestData) {
        log("Processing request: " + requestData);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String response = "PROCESSED[" + serverId + "]: " + requestData;

        return response;
    }

    @Override
    protected void onPromotedToPrimary() {
        log("Completing promotion to primary - initializing primary resources");
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java BackupServer <serverId> <host> <port>");
            System.exit(1);
        }

        try {
            int serverId = Integer.parseInt(args[0]);
            String host = args[1];
            int port = Integer.parseInt(args[2]);

            BackupServer server = new BackupServer(serverId, host, port);
            server.start();

        } catch (Exception e) {
            System.err.println("Error starting backup server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
