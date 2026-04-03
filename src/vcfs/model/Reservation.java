package vcfs.model;

/**
 * Represents a candidate's reservation for an appointment slot.
 * Required for JUnit testing as specified by the assignment.
 */
public class Reservation {

    private String id;
    private Candidate candidate;
    private AppointmentSlot slot;
    private ReservationStatus status;
    private AttendanceRecord attendanceRecord;

    public Reservation(String id, Candidate candidate, AppointmentSlot slot) {
        this.id = id;
        this.candidate = candidate;
        this.slot = slot;
        this.status = ReservationStatus.PENDING;
        this.attendanceRecord = null;
    }

    /**
     * Confirms this reservation, changing status from PENDING to CONFIRMED.
     */
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    /**
     * Cancels this reservation and frees up the appointment slot.
     */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
        slot.cancel();
    }

    /**
     * Attaches an attendance record to this reservation after the session ends.
     */
    public void setAttendanceRecord(AttendanceRecord record) {
        this.attendanceRecord = record;
    }

    // Getters
    public String getId() { return id; }
    public Candidate getCandidate() { return candidate; }
    public AppointmentSlot getSlot() { return slot; }
    public ReservationStatus getStatus() { return status; }
    public AttendanceRecord getAttendanceRecord() { return attendanceRecord; }
}
