package monitor;

import common.ServerInfo;

/** Tracks the last heartbeat time for a server. */
public class HeartbeatRecord {
    private ServerInfo serverInfo;
    private long lastHeartbeatTime;

    public HeartbeatRecord(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        this.lastHeartbeatTime = System.currentTimeMillis();
    }

    public void updateHeartbeat() {
        this.lastHeartbeatTime = System.currentTimeMillis();
    }

    public boolean isAlive(long timeoutMs) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastHeartbeatTime) < timeoutMs;
    }

    public long getTimeSinceLastHeartbeat() {
        return System.currentTimeMillis() - lastHeartbeatTime;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public long getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    @Override
    public String toString() {
        return String.format("HeartbeatRecord[%s, lastSeen=%dms ago]",
                serverInfo, getTimeSinceLastHeartbeat());
    }
}
