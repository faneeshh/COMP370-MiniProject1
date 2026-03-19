package common;

import java.io.Serializable;

/** Server identity and connection info. */
public class ServerInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int serverId;
    private String host;
    private int port;
    private boolean isPrimary;

    public ServerInfo(int serverId, String host, int port, boolean isPrimary) {
        this.serverId = serverId;
        this.host = host;
        this.port = port;
        this.isPrimary = isPrimary;
    }

    public int getServerId() {
        return serverId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    @Override
    public String toString() {
        return String.format("ServerInfo[id=%d, %s:%d, primary=%b]",
                serverId, host, port, isPrimary);
    }
}
