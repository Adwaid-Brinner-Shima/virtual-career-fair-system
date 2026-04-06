package vcfs.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vcfs.model.*;
import static org.junit.jupiter.api.Assertions.*;

public class LobbyTest {

    private Lobby lobby;
    private Candidate candidate1;
    private Candidate candidate2;

    @BeforeEach
    void setUp() {
        Booth booth = new Booth("B001", "Test Booth");
        VirtualRoom room = new VirtualRoom(booth);
        lobby = new Lobby(room);
        candidate1 = new Candidate("C001", "Alice", "alice@test.com");
        candidate2 = new Candidate("C002", "Bob", "bob@test.com");
    }

    @Test
    void testJoinLobby() {
        lobby.join(candidate1);
        assertEquals(1, lobby.getWaitingCandidates().size());
    }

    @Test
    void testNoDuplicateJoin() {
        lobby.join(candidate1);
        lobby.join(candidate1);
        assertEquals(1, lobby.getWaitingCandidates().size());
    }

    @Test
    void testRemoveCandidate() {
        lobby.join(candidate1);
        lobby.join(candidate2);
        lobby.removeCandidate(candidate1);
        assertEquals(1, lobby.getWaitingCandidates().size());
        assertFalse(lobby.getWaitingCandidates().contains(candidate1));
    }

    @Test
    void testLobbyStartsEmpty() {
        assertTrue(lobby.getWaitingCandidates().isEmpty());
    }
}