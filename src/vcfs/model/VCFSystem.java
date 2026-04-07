package vcfs.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The central controller of the Virtual Career Fair System.
 * Manages system state transitions, organisations, candidates, and audit logs.
 * The SystemTimer drives state changes by calling advanceState().
 */
public class VCFSystem {

    private SystemState state;
    private LocalDateTime bookingsOpenTime;
    private LocalDateTime bookingsCloseTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private List<Organisation> organisations;
    private List<Candidate> candidates;
    private List<AuditLog> auditLogs;
    private List<Notification> notifications;
    private FairLive fairLive;

    public VCFSystem() {
        this.state = SystemState.DORMANT;
        this.organisations = new ArrayList<>();
        this.candidates = new ArrayList<>();
        this.auditLogs = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    /**
     * Advances the system to the next appropriate state based on current time.
     * Called by the SystemTimer when a milestone time is reached.
     */
    public void advanceState(LocalDateTime currentTime) {
        switch (state) {
            case DORMANT:
                state = SystemState.PREPARING;
                logEvent("System moved to PREPARING", "System");
                break;
            case PREPARING:
                if (currentTime != null && !currentTime.isBefore(bookingsOpenTime)) {
                    state = SystemState.BOOKINGS_OPEN;
                    logEvent("System moved to BOOKINGS_OPEN", "System");
                }
                break;
            case BOOKINGS_OPEN:
                if (currentTime != null && !currentTime.isBefore(bookingsCloseTime)) {
                    state = SystemState.BOOKINGS_CLOSED;
                    logEvent("System moved to BOOKINGS_CLOSED", "System");
                }
                break;
            case BOOKINGS_CLOSED:
                if (currentTime != null && !currentTime.isBefore(startTime)) {
                    state = SystemState.FAIR_LIVE;
                    openAllRooms();
                    if (fairLive != null) { fairLive.startFair(); }
                    logEvent("System moved to FAIR_LIVE", "System");
                }
                break;
            case FAIR_LIVE:
                if (currentTime != null && !currentTime.isBefore(endTime)) {
                    if (fairLive != null) { fairLive.endFair(); }
                    closeAllRooms();
                    state = SystemState.DORMANT;
                    logEvent("Fair ended. System returned to DORMANT", "System");
                }
                break;
        }
    }

    /**
     * Opens all virtual rooms when the fair goes live.
     */
    private void openAllRooms() {
        for (Organisation org : organisations) {
            for (Booth booth : org.getBooths()) {
                booth.openRoom();
            }
        }
    }

    /**
     * Closes all virtual rooms when the fair ends.
     */
    private void closeAllRooms() {
        for (Organisation org : organisations) {
            for (Booth booth : org.getBooths()) {
                booth.closeRoom();
            }
        }
    }

    /**
     * Logs a significant event to the audit log.
     */
    public void logEvent(String event, String actor) {
        AuditLog log = new AuditLog(event, actor);
        log.record();
        auditLogs.add(log);
    }

    /**
     * Resets all fair data (reservations, sessions, notifications).
     * Used by the administrator in the Dormant state.
     */
    public void resetData() {
        candidates.clear();
        notifications.clear();
        auditLogs.clear();
        logEvent("System data reset by administrator", "Administrator");
    }

    // Registrations
    public void registerCandidate(Candidate candidate) {
        candidates.add(candidate);
        logEvent("Candidate registered: " + candidate.getName(), candidate.getName());
    }

    public void addOrganisation(Organisation org) {
        organisations.add(org);
    }

    // Getters and setters
    public SystemState getState() { return state; }

    public LocalDateTime getBookingsOpenTime() { return bookingsOpenTime; }
    public void setBookingsOpenTime(LocalDateTime t) { this.bookingsOpenTime = t; }

    public LocalDateTime getBookingsCloseTime() { return bookingsCloseTime; }
    public void setBookingsCloseTime(LocalDateTime t) { this.bookingsCloseTime = t; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime t) { this.startTime = t; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime t) { this.endTime = t; }

    public List<Organisation> getOrganisations() { return organisations; }
    public List<Candidate> getCandidates() { return candidates; }
    public List<AuditLog> getAuditLogs() { return auditLogs; }
    public List<Notification> getNotifications() { return notifications; }

    public FairLive getFairLive() { return fairLive; }
    public void setFairLive(FairLive fairLive) { this.fairLive = fairLive; }
}
