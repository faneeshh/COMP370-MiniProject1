package common;

import java.io.Serializable;

/** Message container for network payloads. */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private Object payload;
    private long timestamp;

    public Message(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("Message[type=%s, timestamp=%d, payload=%s]",
                type, timestamp, payload);
    }
}
