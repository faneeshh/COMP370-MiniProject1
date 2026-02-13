package client;

import java.util.Scanner;

/** Client entry point. */
public class ClientMain {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ClientMain <clientId> [interactive]");
            System.err.println("  clientId: Unique identifier for this client");
            System.err.println("  interactive: Optional flag for interactive mode");
            System.exit(1);
        }

        String clientId = args[0];
        boolean interactive = args.length > 1 && args[1].equalsIgnoreCase("interactive");

        Client client = new Client(clientId);

        System.out.println("=== SRMS Client Starting ===");
        System.out.println("Client ID: " + clientId);
        System.out.println("Mode: " + (interactive ? "Interactive" : "Automated"));
        System.out.println("============================");

        if (!client.initialize()) {
            System.err.println("Failed to initialize client - exiting");
            System.exit(1);
        }

        if (interactive) {
            runInteractiveMode(client);
        } else {
            runAutomatedMode(client);
        }

        client.shutdown();
        System.out.println("Client terminated");
    }

    private static void runInteractiveMode(Client client) {
        System.out.println("\nEntering interactive mode...");
        System.out.println("Type requests (or 'quit' to exit):");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                    break;
                }

                if (input.isEmpty()) {
                    continue;
                }

                String response = client.sendRequest(input);

                if (response != null) {
                    System.out.println("Response: " + response);
                } else {
                    System.out.println("ERROR: Request failed");
                }
            }
        }

        System.out.println("Exiting interactive mode");
    }

    private static void runAutomatedMode(Client client) {
        System.out.println("\nEntering automated mode...");

        String[] testRequests = {
                "Process data batch 1",
                "Process data batch 2",
                "Process data batch 3",
                "Compute result for query A",
                "Compute result for query B"
        };

        for (int i = 0; i < testRequests.length; i++) {
            System.out.println("\n--- Request " + (i + 1) + " of " + testRequests.length + " ---");

            String request = testRequests[i];
            System.out.println("Sending: " + request);

            String response = client.sendRequest(request);

            if (response != null) {
                System.out.println("Response: " + response);
            } else {
                System.out.println("ERROR: Request failed");
            }

            if (i < testRequests.length - 1) {
                try {
                    Thread.sleep(2000); // 2 seconds between requests
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        System.out.println("\nAutomated test sequence complete");
    }
}
