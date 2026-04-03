package vcfs.model;

import java.time.LocalDateTime;

/**
 * Represents a live meeting session between a recruiter and a candidate.
 * Sessions are hosted inside a VirtualRoom during the FairLive state.
 * Required for JUnit testing as specified by the assignment.
 */
public class MeetingSession {

    private Recruiter recruiter;
    private Candidate candidate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isActive;

    public MeetingSession(Recruiter recruiter, Candidate candidate,
                          LocalDateTime startTime, LocalDateTime endTime) {
        this.recruiter = recruiter;
        this.candidate = candidate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = false;
    }

    /**
     * Starts the meeting session, marking it as active.
     */
    public void start() {
        this.isActive = true;
        System.out.println("[MeetingSession] Session started between "
            + recruiter.getName() + " and " + candidate.getName());
    }

    /**
     * Ends the meeting session, marking it as inactive.
     */
    public void end() {
        this.isActive = false;
        System.out.println("[MeetingSession] Session ended between "
            + recruiter.getName() + " and " + candidate.getName());
    }

    // Getters
    public Recruiter getRecruiter() { return recruiter; }
    public Candidate getCandidate() { return candidate; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public boolean isActive() { return isActive; }
}
