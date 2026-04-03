package vcfs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a job or internship offer published by a recruiter at a booth.
 * Offers have a duration and capacity which determine how slots are generated.
 */
public class Offer {

    private String id;
    private String title;
    private List<String> tags;
    private int duration;   // duration of each appointment in minutes
    private int capacity;   // max candidates per slot

    public Offer(String id, String title, int duration, int capacity) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.capacity = capacity;
        this.tags = new ArrayList<>();
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    // Getters and setters
    public String getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getTags() { return tags; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
