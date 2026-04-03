package vcfs.timer;

import vcfs.model.VCFSystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulates the passage of time for the Virtual Career Fair System.
 * Drives state transitions by notifying VCFSystem when milestone times are reached.
 * This is implemented as a separate screen as required by the specification.
 */
public class SystemTimer {

    private LocalDateTime currentTime;
    private VCFSystem system;
    private List<TimerListener> listeners;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public SystemTimer(VCFSystem system, LocalDateTime initialTime) {
        this.system = system;
        this.currentTime = initialTime;
        this.listeners = new ArrayList<>();
    }

    /**
     * Skips forward by the given number of minutes.
     * Used for micro-skips (e.g. 5 minutes at a time).
     */
    public void skipMinutes(int n) {
        currentTime = currentTime.plusMinutes(n);
        notifyAll(currentTime);
        system.advanceState(currentTime);
    }

    /**
     * Skips forward by exactly one day.
     * Used for macro-skips during testing.
     */
    public void skipDay() {
        currentTime = currentTime.plusDays(1);
        notifyAll(currentTime);
        system.advanceState(currentTime);
    }

    /**
     * Jumps directly to a specific target date and time.
     * Useful for jumping to milestone times (e.g. BookingsOpen, FairStart).
     */
    public void jumpTo(LocalDateTime target) {
        if (target.isBefore(currentTime)) {
            System.out.println("[SystemTimer] Cannot go back in time.");
            return;
        }
        currentTime = target;
        notifyAll(currentTime);
        system.advanceState(currentTime);
    }

    /**
     * Jumps directly to the BookingsOpen time configured in VCFSystem.
     */
    public void jumpToBookingsOpen() {
        if (system.getBookingsOpenTime() != null) {
            jumpTo(system.getBookingsOpenTime());
        }
    }

    /**
     * Jumps directly to the BookingsClosed time configured in VCFSystem.
     */
    public void jumpToBookingsClosed() {
        if (system.getBookingsCloseTime() != null) {
            jumpTo(system.getBookingsCloseTime());
        }
    }

    /**
     * Jumps directly to the FairLive start time configured in VCFSystem.
     */
    public void jumpToFairStart() {
        if (system.getStartTime() != null) {
            jumpTo(system.getStartTime());
        }
    }

    /**
     * Jumps directly to the FairEnd time configured in VCFSystem.
     */
    public void jumpToFairEnd() {
        if (system.getEndTime() != null) {
            jumpTo(system.getEndTime());
        }
    }

    /**
     * Registers a listener that gets notified whenever time changes.
     * Used by the SystemTimerScreen UI to update its display.
     */
    public void addListener(TimerListener listener) {
        listeners.add(listener);
    }

    private void notifyAll(LocalDateTime time) {
        for (TimerListener listener : listeners) {
            listener.onTimeChanged(time);
        }
        System.out.println("[SystemTimer] Time is now: " + time.format(FORMATTER)
            + " | System state: " + system.getState());
    }

    // Getters
    public LocalDateTime getCurrentTime() { return currentTime; }

    public String getFormattedTime() { return currentTime.format(FORMATTER); }
}
