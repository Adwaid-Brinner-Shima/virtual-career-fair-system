package vcfs.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vcfs.model.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class MeetingSessionTest {

    private MeetingSession session;

    @BeforeEach
    void setUp() {
        Recruiter recruiter = new Recruiter("R001", "John", "john@company.com");
        Candidate candidate = new Candidate("C001", "Alice", "alice@test.com");
        session = new MeetingSession(recruiter, candidate,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
    }

    @Test
    void testSessionStartsInactive() {
        assertFalse(session.isActive());
    }

    @Test
    void testSessionStart() {
        session.start();
        assertTrue(session.isActive());
    }

    @Test
    void testSessionEnd() {
        session.start();
        session.end();
        assertFalse(session.isActive());
    }
}