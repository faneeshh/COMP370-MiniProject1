package server;

import common.Message;
import common.MessageType;
import java.io.*;
import java.net.Socket;

/** Primary server implementation. */
public class PrimaryServer extends ServerProcess {

    public PrimaryServer(int serverId, String host, int port) {
        super(serverId, host, port, true); // Starts as primary
    }

    @Override
    protected void handleClient(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            Message request = (Message) in.readObject();
            log("Received request: " + request);

            if (request.getType() == MessageType.PROCESS) {
                String requestData = (String) request.getPayload();
                String responseData = processRequest(requestData);

                Message response = new Message(MessageType.RESPONSE, responseData);
                out.writeObject(response);
                out.flush();

                log("Sent response: " + responseData);
            } else if (request.getType() == MessageType.PROMOTE) {
                log("Received PROMOTE message");
                promoteToprimary();
            } else {
                log("Unknown message type: " + request.getType());
            }

        } catch (Exception e) {
            log("Error handling client: " + e.getMessage());
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

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java PrimaryServer <serverId> <host> <port>");
            System.exit(1);
        }

        try {
            int serverId = Integer.parseInt(args[0]);
            String host = args[1];
            int port = Integer.parseInt(args[2]);

            PrimaryServer server = new PrimaryServer(serverId, host, port);
            server.start();

        } catch (Exception e) {
            System.err.println("Error starting primary server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
