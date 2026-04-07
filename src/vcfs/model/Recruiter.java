package vcfs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recruiter participating in the Virtual Career Fair.
 * Recruiters publish offers, define availability, and host meeting sessions.
 */
public class Recruiter {

    private String id;
    private String name;
    private String email;
    private List<Offer> offers;
    private List<AvailabilityBlock> availabilityBlocks;
    private List<Notification> notifications;

    public Recruiter(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.offers = new ArrayList<>();
        this.availabilityBlocks = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    /**
     * Publishes an offer so candidates can book appointments for it.
     */
    public void publishOffer(Offer offer) {
        offers.add(offer);
    }

    /**
     * Cancels a candidate's reservation if the recruiter's availability changes.
     */
    public void cancelAppointment(Reservation reservation) {
        reservation.cancel();
    }

    /**
     * Adds an availability block for this recruiter during the Preparing state.
     */
    public void addAvailabilityBlock(AvailabilityBlock block) {
        availabilityBlocks.add(block);
    }

    // Getters and setters
    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Offer> getOffers() { return offers; }

    public List<AvailabilityBlock> getAvailabilityBlocks() { return availabilityBlocks; }

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    public List<Notification> getNotifications() { return notifications; }
}
