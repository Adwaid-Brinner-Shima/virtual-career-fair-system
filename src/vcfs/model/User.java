package vcfs.model;
 
/**
 * Abstract base for all users (Candidate and Recruiter) in the UI layer.
 */
public abstract class User {
 
    protected String id;
    protected String displayName;
    protected String email;
 
    protected User(String id, String displayName, String email) {
        this.id          = id;
        this.displayName = displayName;
        this.email       = email;
    }
 
    /** Returns the internal user ID. */
    public String getId() { return id; }
 
    /** Returns the user's display name. */
    public String getDisplayName() { return displayName; }
 
    /** Returns the user's unique email identifier. */
    public String getEmail() { return email; }
}