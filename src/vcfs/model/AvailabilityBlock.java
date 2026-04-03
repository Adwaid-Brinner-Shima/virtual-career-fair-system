package vcfs.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a continuous block of time a recruiter is available.
 * e.g. 10:00-12:00. The system splits this into individual AppointmentSlots
 * based on the offer's duration.
 */
public class AvailabilityBlock {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Recruiter recruiter;
    private List<AppointmentSlot> slots;

    public AvailabilityBlock(LocalDateTime startTime, LocalDateTime endTime, Recruiter recruiter) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.recruiter = recruiter;
        this.slots = new ArrayList<>();
    }

    /**
     * Splits this availability block into individual appointment slots
     * based on the given offer duration in minutes.
     */
    public void generateSlots(Offer offer) {
        slots.clear();
        LocalDateTime current = startTime;
        int durationMins = offer.getDuration();

        while (!current.plusMinutes(durationMins).isAfter(endTime)) {
            LocalDateTime slotEnd = current.plusMinutes(durationMins);
            AppointmentSlot slot = new AppointmentSlot(current, slotEnd, recruiter, offer);
            slots.add(slot);
            current = slotEnd;
        }
    }

    public List<AppointmentSlot> getSlots() { return slots; }

    // Getters and setters
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Recruiter getRecruiter() { return recruiter; }
}
