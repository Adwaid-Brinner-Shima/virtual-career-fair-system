package vcfs.model;

import java.time.LocalDateTime;

/**
 * Logs the attendance outcome of a candidate at the end of a meeting session.
 * Generated automatically when a session ends during the FairLive state.
 */
public class AttendanceRecord {

    private LocalDateTime joinTime;
    private LocalDateTime leaveTime;
    private AttendOutcome outcome;

    public AttendanceRecord(LocalDateTime joinTime, LocalDateTime leaveTime, AttendOutcome outcome) {
        this.joinTime = joinTime;
        this.leaveTime = leaveTime;
        this.outcome = outcome;
    }

    /**
     * Logs this attendance record (e.g. to console or persistent store).
     */
    public void log() {
        System.out.println("[AttendanceRecord] Outcome: " + outcome
            + " | Joined: " + joinTime
            + " | Left: " + leaveTime);
    }

    // Getters
    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }

    public LocalDateTime getLeaveTime() { return leaveTime; }
    public void setLeaveTime(LocalDateTime leaveTime) { this.leaveTime = leaveTime; }

    public AttendOutcome getOutcome() { return outcome; }
    public void setOutcome(AttendOutcome outcome) { this.outcome = outcome; }
}
