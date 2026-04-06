package vcfs.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vcfs.model.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        Candidate candidate = new Candidate("C001", "Alice", "alice@test.com");
        Recruiter recruiter = new Recruiter("R001", "John", "john@company.com");
        Offer offer = new Offer("O001", "Software Engineer", 30, 1);
        AppointmentSlot slot = new AppointmentSlot(
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30),
                recruiter,
                offer
        );
        reservation = new Reservation("R001", candidate, slot);
    }

    @Test
    void testInitialStatusIsPending() {
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    }

    @Test
    void testConfirmReservation() {
        reservation.confirm();
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
    }

    @Test
    void testCancelReservation() {
        reservation.cancel();
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }
}