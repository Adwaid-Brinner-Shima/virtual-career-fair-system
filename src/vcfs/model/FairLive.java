package vcfs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the live fair — rooms, lobbies, and active sessions.
 * Activated when VCFSystem transitions to FAIR_LIVE state.
 */
public class FairLive {

    private VCFSystem system;
    private List<VirtualRoom> activeRooms;

    public FairLive(VCFSystem system) {
        this.system = system;
        this.activeRooms = new ArrayList<>();
    }

    /**
     * Opens all virtual rooms for all booths when fair goes live.
     */
    public void startFair() {
        for (Organisation org : system.getOrganisations()) {
            for (Booth booth : org.getBooths()) {
                booth.openRoom();
                VirtualRoom room = new VirtualRoom(booth);
                room.open();
                activeRooms.add(room);
                system.logEvent("VirtualRoom opened for booth: " + booth.getName(), "System");
            }
        }
    }

    /**
     * Closes all virtual rooms when the fair ends.
     */
    public void endFair() {
        for (VirtualRoom room : activeRooms) {
            room.close();
            system.logEvent("VirtualRoom closed for booth: "
                    + room.getBooth().getName(), "System");
        }
        activeRooms.clear();
    }

    /**
     * Sends a candidate to the lobby of the correct room.
     */
    public void sendToLobby(Candidate candidate, VirtualRoom room) {
        room.getLobby().join(candidate);
        system.logEvent(candidate.getName() + " joined lobby for booth: "
                + room.getBooth().getName(), candidate.getName());
    }

    /**
     * Starts a meeting session in a virtual room.
     */
    public void startSession(VirtualRoom room, MeetingSession session) {
        room.setCurrentSession(session);
        session.start();
        system.logEvent("Session started in booth: "
                + room.getBooth().getName(), session.getRecruiter().getName());
    }

    /**
     * Ends the current session and admits next from lobby.
     */
    public void endSession(VirtualRoom room) {
        MeetingSession session = room.getCurrentSession();
        if (session != null && session.isActive()) {
            session.end();
            system.logEvent("Session ended in booth: "
                    + room.getBooth().getName(), session.getRecruiter().getName());
        }
        room.getLobby().admitNext();
    }

    // Getters
    public List<VirtualRoom> getActiveRooms() { return activeRooms; }
}