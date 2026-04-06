package vcfs.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles attendance recording and audit logging for sessions.
 */
public class AttendanceLogger {

    private VCFSystem system;
    private List<AttendanceRecord> records;

    public AttendanceLogger(VCFSystem system) {
        this.system = system;
        this.records = new ArrayList<>();
    }

    /**
     * Records attendance when a session ends.
     */
    public AttendanceRecord recordAttendance(LocalDateTime joinTime,
                                             LocalDateTime leaveTime,
                                             AttendOutcome outcome,
                                             Reservation reservation) {
        AttendanceRecord record = new AttendanceRecord(joinTime, leaveTime, outcome);
        record.log();
        records.add(record);
        reservation.setAttendanceRecord(record);
        system.logEvent("Attendance recorded: " + outcome
                        + " for " + reservation.getCandidate().getName(),
                reservation.getCandidate().getName());
        return record;
    }

    /**
     * Logs a no-show when a candidate misses their session.
     */
    public void recordNoShow(Reservation reservation, LocalDateTime sessionTime) {
        AttendanceRecord record = new AttendanceRecord(sessionTime, sessionTime,
                AttendOutcome.NO_SHOW);
        record.log();
        records.add(record);
        reservation.setAttendanceRecord(record);
        system.logEvent("No-show recorded for: "
                        + reservation.getCandidate().getName(),
                "System");
    }

    // Getters
    public List<AttendanceRecord> getRecords() { return records; }
}