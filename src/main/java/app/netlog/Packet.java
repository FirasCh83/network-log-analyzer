package app.netlog;
import java.io.Serializable;
import java.time.Instant;

public final class Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Instant timestamp;
    public final String source, destination, protocol;
    public final long bytes;
    public Packet(Instant timestamp, String source, String destination, String protocol, long bytes) {
        if (timestamp == null || source == null || destination == null || protocol == null || bytes < 0) throw new IllegalArgumentException("Invalid packet fields");
        this.timestamp=timestamp; this.source=source.trim(); this.destination=destination.trim(); this.protocol=protocol.trim().toUpperCase(); this.bytes=bytes;
    }
    public String toString(){ return timestamp+"  "+source+" -> "+destination+"  "+protocol+"  "+bytes+" bytes"; }
}
