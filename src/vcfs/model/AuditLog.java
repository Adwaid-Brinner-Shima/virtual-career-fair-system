package vcfs.model;

import java.time.LocalDateTime;

/**
 * Represents an audit log entry for a significant event during the fair.
 * The administrator can view these logs via the AdministratorScreen.
 */
public class AuditLog {

    private String event;
    private LocalDateTime timestamp;
    private String actor;

    public AuditLog(String event, String actor) {
        this.event = event;
        this.actor = actor;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Records this audit log entry to the system output.
     */
    public void record() {
        System.out.println("[AuditLog] " + timestamp + " | Actor: " + actor + " | Event: " + event);
    }

    // Getters
    public String getEvent() { return event; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getActor() { return actor; }
}
