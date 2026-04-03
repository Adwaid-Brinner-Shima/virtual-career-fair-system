package vcfs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an organisation participating in the Virtual Career Fair.
 * An organisation owns one or more booths at the fair.
 */
public class Organisation {

    private String id;
    private String name;
    private String description;
    private List<Booth> booths;

    public Organisation(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.booths = new ArrayList<>();
    }

    public void addBooth(Booth booth) {
        booths.add(booth);
    }

    public void removeBooth(Booth booth) {
        booths.remove(booth);
    }

    // Getters and setters
    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Booth> getBooths() { return booths; }
}
