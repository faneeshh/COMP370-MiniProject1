package monitor;

/** Monitor entry point. */
public class MonitorMain {

    private static Monitor monitor;

    public static void main(String[] args) {
        System.out.println("=== SRMS Monitor Starting ===");
        System.out.println("Initializing monitor service...");
        System.out.println("============================\n");

        monitor = new Monitor();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=== Shutdown signal received ===");
            if (monitor != null) {
                monitor.shutdown();
            }
        }));

        try {
            monitor.start();

        } catch (Exception e) {
            System.err.println("FATAL: Monitor failed to start");
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("\n=== Monitor Terminated ===");
    }
}
