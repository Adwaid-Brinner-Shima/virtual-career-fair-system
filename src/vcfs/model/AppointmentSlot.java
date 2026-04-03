package vcfs.model;

import java.time.LocalDateTime;

/**
 * Represents a single bookable appointment window generated from an AvailabilityBlock.
 * Candidates can book these slots during the BookingsOpen state.
 */
public class AppointmentSlot {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isAvailable;
    private Recruiter recruiter;
    private Offer offer;
    private Reservation reservation;

    public AppointmentSlot(LocalDateTime startTime, LocalDateTime endTime,
                           Recruiter recruiter, Offer offer) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.recruiter = recruiter;
        this.offer = offer;
        this.isAvailable = true;
        this.reservation = null;
    }

    /**
     * Books this slot for the given candidate, creating a reservation.
     * @return the created Reservation, or null if the slot is unavailable.
     */
    public Reservation book(Candidate candidate) {
        if (!isAvailable) return null;

        reservation = new Reservation(
            java.util.UUID.randomUUID().toString(),
            candidate,
            this
        );
        isAvailable = false;
        return reservation;
    }

    /**
     * Cancels the booking on this slot, making it available again.
     */
    public void cancel() {
        this.reservation = null;
        this.isAvailable = true;
    }

    // Getters
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public boolean isAvailable() { return isAvailable; }
    public Recruiter getRecruiter() { return recruiter; }
    public Offer getOffer() { return offer; }
    public Reservation getReservation() { return reservation; }
}
