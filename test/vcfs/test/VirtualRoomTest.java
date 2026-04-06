package vcfs.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vcfs.model.*;
import static org.junit.jupiter.api.Assertions.*;

public class VirtualRoomTest {

    private VirtualRoom room;

    @BeforeEach
    void setUp() {
        Booth booth = new Booth("B001", "Test Booth");
        room = new VirtualRoom(booth);
    }

    @Test
    void testRoomStartsClosed() {
        assertFalse(room.isOpen());
    }

    @Test
    void testOpenRoom() {
        room.open();
        assertTrue(room.isOpen());
    }

    @Test
    void testCloseRoom() {
        room.open();
        room.close();
        assertFalse(room.isOpen());
    }

    @Test
    void testLobbyNotNull() {
        assertNotNull(room.getLobby());
    }
}