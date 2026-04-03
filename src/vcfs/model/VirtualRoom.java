package vcfs.model;

/**
 * Represents the virtual meeting room for a booth during the FairLive state.
 * Each booth has one VirtualRoom. It manages the current session and the lobby.
 * Required for JUnit testing as specified by the assignment.
 */
public class VirtualRoom {

    private Booth booth;
    private boolean isOpen;
    private MeetingSession currentSession;
    private Lobby lobby;

    public VirtualRoom(Booth booth) {
        this.booth = booth;
        this.isOpen = false;
        this.currentSession = null;
        this.lobby = new Lobby(this);
    }

    /**
     * Opens this virtual room when the fair goes live.
     */
    public void open() {
        this.isOpen = true;
        System.out.println("[VirtualRoom] Room opened for booth: " + booth.getName());
    }

    /**
     * Closes this virtual room when the fair ends.
     */
    public void close() {
        if (currentSession != null && currentSession.isActive()) {
            currentSession.end();
        }
        this.isOpen = false;
        System.out.println("[VirtualRoom] Room closed for booth: " + booth.getName());
    }

    /**
     * Ends the current session and starts the next one from the lobby.
     */
    public void startNextSession() {
        if (currentSession != null && currentSession.isActive()) {
            currentSession.end();
        }
        currentSession = null;
        System.out.println("[VirtualRoom] Ready for next session in booth: " + booth.getName());
    }

    /**
     * Sets the current active meeting session for this room.
     */
    public void setCurrentSession(MeetingSession session) {
        this.currentSession = session;
    }

    // Getters
    public Booth getBooth() { return booth; }
    public boolean isOpen() { return isOpen; }
    public MeetingSession getCurrentSession() { return currentSession; }
    public Lobby getLobby() { return lobby; }
}
