package vcfs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a candidate registered for the Virtual Career Fair.
 * Candidates browse offers, make reservations, and attend meeting sessions.
 */
public class Candidate {

    private String id;
    private String name;
    private String email;
    private List<Reservation> reservations;
    private List<Notification> notifications;

    public Candidate(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.reservations = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    /**
     * Registers the candidate into the system using their name and email.
     */
    public void register() {
        // Registration logic handled by VCFSystem
    }

    /**
     * Cancels one of the candidate's existing reservations.
     */
    public void cancelReservation(Reservation reservation) {
        reservation.cancel();
        reservations.remove(reservation);
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    // Getters and setters
    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Reservation> getReservations() { return reservations; }

    public List<Notification> getNotifications() { return notifications; }
}
