package vcfs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the waiting lobby for a VirtualRoom.
 * Candidates who arrive early for their session wait here until
 * their appointment window begins.
 * Required for JUnit testing as specified by the assignment.
 */
public class Lobby {

    private VirtualRoom virtualRoom;
    private List<Candidate> waitingCandidates;

    public Lobby(VirtualRoom virtualRoom) {
        this.virtualRoom = virtualRoom;
        this.waitingCandidates = new ArrayList<>();
    }

    /**
     * Adds a candidate to the waiting lobby.
     */
    public void join(Candidate candidate) {
        if (!waitingCandidates.contains(candidate)) {
            waitingCandidates.add(candidate);
            System.out.println("[Lobby] " + candidate.getName() + " is waiting in the lobby.");
        }
    }

    /**
     * Admits the next candidate from the lobby into the virtual room.
     */
    public void admitNext() {
        if (!waitingCandidates.isEmpty()) {
            Candidate next = waitingCandidates.remove(0);
            System.out.println("[Lobby] Admitting " + next.getName() + " into the session.");
            virtualRoom.startNextSession();
        }
    }

    /**
     * Removes a specific candidate from the lobby (e.g. if they leave early).
     */
    public void removeCandidate(Candidate candidate) {
        waitingCandidates.remove(candidate);
        System.out.println("[Lobby] " + candidate.getName() + " removed from lobby.");
    }

    // Getters
    public List<Candidate> getWaitingCandidates() { return waitingCandidates; }
    public VirtualRoom getVirtualRoom() { return virtualRoom; }
}
