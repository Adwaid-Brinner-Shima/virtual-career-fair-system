package vcfs.model;

/**
 * Represents the possible states of the Virtual Career Fair System.
 * The system transitions through these states in order.
 */
public enum SystemState {
    DORMANT,
    PREPARING,
    BOOKINGS_OPEN,
    BOOKINGS_CLOSED,
    FAIR_LIVE
}
