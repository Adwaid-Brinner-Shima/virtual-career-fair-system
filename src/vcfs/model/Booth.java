package vcfs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a booth at the Virtual Career Fair.
 * A booth belongs to an organisation and hosts one or more recruiters and offers.
 */
public class Booth {

    private String id;
    private String name;
    private boolean isRoomOpen;
    private List<Recruiter> recruiters;
    private List<Offer> offers;

    public Booth(String id, String name) {
        this.id = id;
        this.name = name;
        this.isRoomOpen = false;
        this.recruiters = new ArrayList<>();
        this.offers = new ArrayList<>();
    }

    /**
     * Opens the virtual room for this booth when the fair goes live.
     */
    public void openRoom() {
        this.isRoomOpen = true;
    }

    /**
     * Closes the virtual room for this booth.
     */
    public void closeRoom() {
        this.isRoomOpen = false;
    }

    public void addRecruiter(Recruiter recruiter) {
        recruiters.add(recruiter);
    }

    public void addOffer(Offer offer) {
        offers.add(offer);
    }

    // Getters and setters
    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isRoomOpen() { return isRoomOpen; }

    public List<Recruiter> getRecruiters() { return recruiters; }

    public List<Offer> getOffers() { return offers; }
}
