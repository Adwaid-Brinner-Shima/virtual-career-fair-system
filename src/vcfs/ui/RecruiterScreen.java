package vcfs.ui;

import vcfs.model.*;
import vcfs.timer.SystemTimer;
import vcfs.timer.TimerListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Recruiter-facing screen for the Virtual Career Fair System.
 * Handles login, offer publishing, availability setup, schedule management,
 * live session hosting, attendance recording, and notifications.
 * Swaps between views automatically as the system state changes.
 */
public class RecruiterScreen extends JFrame implements TimerListener {

    private final VCFSystem system;
    private final SystemTimer timer;
    private final FairLive fairLive;
    private final AttendanceLogger attendanceLogger;

    private Recruiter recruiter;       // set after login
    private Booth recruiterBooth;      // the booth this recruiter belongs to

    // header labels
    private JLabel timeLabel;
    private JLabel stateLabel;

    // the main content area that swaps between views
    private JPanel contentPanel;
    private CardLayout contentLayout;

    // table models for the different views
    private DefaultTableModel scheduleModel;
    private DefaultTableModel notificationsModel;

    // tracks which slots map to which table rows in the schedule view
    private final List<AppointmentSlot> scheduleSlotCache = new ArrayList<>();

    // live session tracking
    private VirtualRoom myRoom;
    private MeetingSession currentSession;
    private Reservation currentReservation;
    private JLabel sessionStatusLabel;
    private JPanel sessionControlPanel;
    private JLabel lobbyCountLabel;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // view card names
    private static final String VIEW_LOGIN           = "LOGIN";
    private static final String VIEW_PREPARING       = "PREPARING";
    private static final String VIEW_BOOKINGS_OPEN   = "BOOKINGS_OPEN";
    private static final String VIEW_BOOKINGS_CLOSED = "BOOKINGS_CLOSED";
    private static final String VIEW_FAIR_LIVE       = "FAIR_LIVE";

    public RecruiterScreen(VCFSystem system, SystemTimer timer, FairLive fairLive) {
        this.system = system;
        this.timer = timer;
        this.fairLive = fairLive;
        this.attendanceLogger = new AttendanceLogger(system);
        timer.addListener(this);
        buildUI();
    }

    public RecruiterScreen(VCFSystem system, SystemTimer timer) {
        this(system, timer, null);
    }

    // ── UI setup ─────────────────────────────────────────────────────────

    private void buildUI() {
        setTitle("VCFS — Recruiter");
        setSize(820, 640);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CandidateUI.BG);
        root.add(buildHeader(), BorderLayout.NORTH);

        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(CandidateUI.BG);

        contentPanel.add(buildLoginView(),          VIEW_LOGIN);
        contentPanel.add(buildPreparingView(),       VIEW_PREPARING);
        contentPanel.add(buildBookingsOpenView(),    VIEW_BOOKINGS_OPEN);
        contentPanel.add(buildBookingsClosedView(),  VIEW_BOOKINGS_CLOSED);
        contentPanel.add(buildFairLiveView(),        VIEW_FAIR_LIVE);

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);

        // start on the login screen
        contentLayout.show(contentPanel, VIEW_LOGIN);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CandidateUI.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Recruiter Panel");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(CandidateUI.WHITE);

        JPanel rightInfo = new JPanel(new GridLayout(2, 1));
        rightInfo.setBackground(CandidateUI.PRIMARY);

        timeLabel = new JLabel("Time: --", SwingConstants.RIGHT);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(148, 163, 184));

        stateLabel = new JLabel("State: " + system.getState(), SwingConstants.RIGHT);
        stateLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        stateLabel.setForeground(new Color(134, 239, 172));

        rightInfo.add(timeLabel);
        rightInfo.add(stateLabel);

        header.add(title, BorderLayout.WEST);
        header.add(rightInfo, BorderLayout.EAST);
        return header;
    }

    // ── LOGIN VIEW ───────────────────────────────────────────────────────

    private JPanel buildLoginView() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CandidateUI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        panel.add(CandidateUI.sectionLabel("Recruiter Login"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(CandidateUI.infoLabel("Select your name from the list of registered recruiters."));
        panel.add(Box.createVerticalStrut(20));

        JComboBox<String> recruiterDropdown = new JComboBox<>();
        recruiterDropdown.setFont(new Font("SansSerif", Font.PLAIN, 13));
        recruiterDropdown.setMaximumSize(new Dimension(400, 32));
        recruiterDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);

        // button to refresh the dropdown in case recruiters were added after this screen opened
        JButton refreshList = CandidateUI.accentButton("Refresh List");
        refreshList.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshList.addActionListener(e -> {
            recruiterDropdown.removeAllItems();
            for (Organisation org : system.getOrganisations()) {
                for (Booth booth : org.getBooths()) {
                    for (Recruiter rec : booth.getRecruiters()) {
                        recruiterDropdown.addItem(rec.getName() + "  (" + rec.getEmail() + ")");
                    }
                }
            }
            if (recruiterDropdown.getItemCount() == 0) {
                CandidateUI.err(this, "No recruiters found. Ask the administrator to add recruiters first.");
            }
        });

        JButton loginBtn = CandidateUI.accentButton("Login");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> {
            if (recruiterDropdown.getItemCount() == 0 || recruiterDropdown.getSelectedIndex() < 0) {
                CandidateUI.err(this, "Please refresh the list and select a recruiter.");
                return;
            }
            // find the recruiter that matches the selected index
            int selectedIndex = recruiterDropdown.getSelectedIndex();
            int counter = 0;
            Recruiter found = null;
            Booth foundBooth = null;
            for (Organisation org : system.getOrganisations()) {
                for (Booth booth : org.getBooths()) {
                    for (Recruiter rec : booth.getRecruiters()) {
                        if (counter == selectedIndex) {
                            found = rec;
                            foundBooth = booth;
                        }
                        counter++;
                    }
                }
            }
            if (found == null) {
                CandidateUI.err(this, "Could not find that recruiter. Try refreshing.");
                return;
            }
            recruiter = found;
            recruiterBooth = foundBooth;
            setTitle("VCFS — Recruiter: " + recruiter.getName());
            system.logEvent("Recruiter logged in: " + recruiter.getName(), recruiter.getName());
            showViewForCurrentState();
        });

        panel.add(refreshList);
        panel.add(Box.createVerticalStrut(12));
        panel.add(recruiterDropdown);
        panel.add(Box.createVerticalStrut(16));
        panel.add(loginBtn);

        return panel;
    }

    // ── PREPARING VIEW ───────────────────────────────────────────────────
    // Recruiter publishes offers and defines availability blocks here.

    private JPanel buildPreparingView() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CandidateUI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ── Offer publishing section ──
        panel.add(CandidateUI.sectionLabel("Publish an Offer"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(CandidateUI.infoLabel("Create offers that candidates can book appointments for."));
        panel.add(Box.createVerticalStrut(12));

        JTextField titleField    = CandidateUI.styledField("");
        JTextField tagsField     = CandidateUI.styledField("");
        JTextField durationField = CandidateUI.styledField("30");
        JTextField capacityField = CandidateUI.styledField("1");

        panel.add(CandidateUI.labelledField("Offer Title", titleField));
        panel.add(Box.createVerticalStrut(6));
        panel.add(CandidateUI.labelledField("Tags (comma separated)", tagsField));
        panel.add(Box.createVerticalStrut(6));
        panel.add(CandidateUI.labelledField("Duration (minutes)", durationField));
        panel.add(Box.createVerticalStrut(6));
        panel.add(CandidateUI.labelledField("Capacity per slot", capacityField));
        panel.add(Box.createVerticalStrut(12));

        JButton addOfferBtn = CandidateUI.accentButton("Add Offer");
        addOfferBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addOfferBtn.addActionListener(e -> {
            if (recruiter == null) { CandidateUI.err(this, "Not logged in."); return; }

            String title = titleField.getText().trim();
            String tagsRaw = tagsField.getText().trim();
            if (title.isEmpty()) { CandidateUI.err(this, "Please enter an offer title."); return; }

            int duration;
            int capacity;
            try {
                duration = Integer.parseInt(durationField.getText().trim());
                capacity = Integer.parseInt(capacityField.getText().trim());
            } catch (NumberFormatException ex) {
                CandidateUI.err(this, "Duration and capacity must be whole numbers.");
                return;
            }
            if (duration <= 0 || capacity <= 0) {
                CandidateUI.err(this, "Duration and capacity must be greater than zero.");
                return;
            }

            Offer offer = new Offer(UUID.randomUUID().toString(), title, duration, capacity);
            if (!tagsRaw.isEmpty()) {
                for (String tag : tagsRaw.split(",")) {
                    String trimmed = tag.trim();
                    if (!trimmed.isEmpty()) {
                        offer.addTag(trimmed);
                    }
                }
            }
            recruiter.publishOffer(offer);
            recruiterBooth.addOffer(offer);
            system.logEvent("Offer published: " + title + " by " + recruiter.getName(),
                    recruiter.getName());

            // clear the fields so they can add another
            titleField.setText("");
            tagsField.setText("");
            durationField.setText("30");
            capacityField.setText("1");
            CandidateUI.ok(this, "Offer published: " + title);
        });
        panel.add(addOfferBtn);

        panel.add(Box.createVerticalStrut(24));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(16));

        // ── Availability block section ──
        panel.add(CandidateUI.sectionLabel("Define Availability Block"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(CandidateUI.infoLabel(
                "Set a time window for an offer. The system splits it into appointment slots automatically."));
        panel.add(Box.createVerticalStrut(12));

        JTextField startField = CandidateUI.styledField("");
        JTextField endField   = CandidateUI.styledField("");
        panel.add(CandidateUI.labelledField("Start (dd-MM-yyyy HH:mm)", startField));
        panel.add(Box.createVerticalStrut(6));
        panel.add(CandidateUI.labelledField("End (dd-MM-yyyy HH:mm)", endField));
        panel.add(Box.createVerticalStrut(6));

        // dropdown to pick which offer this availability block is for
        JComboBox<String> offerDropdown = new JComboBox<>();
        offerDropdown.setFont(new Font("SansSerif", Font.PLAIN, 13));
        offerDropdown.setMaximumSize(new Dimension(400, 32));
        offerDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton refreshOffers = CandidateUI.accentButton("Refresh Offers");
        refreshOffers.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshOffers.addActionListener(e -> {
            offerDropdown.removeAllItems();
            if (recruiter != null) {
                for (Offer o : recruiter.getOffers()) {
                    offerDropdown.addItem(o.getTitle() + "  (" + o.getDuration() + " min)");
                }
            }
            if (offerDropdown.getItemCount() == 0) {
                CandidateUI.err(this, "No offers yet. Publish an offer first.");
            }
        });

        panel.add(refreshOffers);
        panel.add(Box.createVerticalStrut(6));
        panel.add(offerDropdown);
        panel.add(Box.createVerticalStrut(12));

        JButton addBlockBtn = CandidateUI.accentButton("Add Availability Block");
        addBlockBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBlockBtn.addActionListener(e -> {
            if (recruiter == null) { CandidateUI.err(this, "Not logged in."); return; }
            if (offerDropdown.getItemCount() == 0 || offerDropdown.getSelectedIndex() < 0) {
                CandidateUI.err(this, "Select an offer first. Click 'Refresh Offers' if the list is empty.");
                return;
            }

            LocalDateTime blockStart;
            LocalDateTime blockEnd;
            try {
                blockStart = LocalDateTime.parse(startField.getText().trim(), FMT);
                blockEnd   = LocalDateTime.parse(endField.getText().trim(), FMT);
            } catch (DateTimeParseException ex) {
                CandidateUI.err(this, "Invalid date format. Use dd-MM-yyyy HH:mm (e.g. 06-04-2026 10:00)");
                return;
            }
            if (!blockEnd.isAfter(blockStart)) {
                CandidateUI.err(this, "End time must be after start time.");
                return;
            }

            Offer selectedOffer = recruiter.getOffers().get(offerDropdown.getSelectedIndex());
            AvailabilityBlock block = new AvailabilityBlock(blockStart, blockEnd, recruiter);
            block.generateSlots(selectedOffer);
            recruiter.addAvailabilityBlock(block);

            int slotsCreated = block.getSlots().size();
            system.logEvent("Availability block added by " + recruiter.getName()
                    + ": " + blockStart.format(FMT) + " to " + blockEnd.format(FMT)
                    + " (" + slotsCreated + " slots)", recruiter.getName());

            startField.setText("");
            endField.setText("");
            CandidateUI.ok(this, "Availability block created with " + slotsCreated + " appointment slots.");
        });
        panel.add(addBlockBtn);

        // wrap in a scroll pane so everything is reachable on smaller screens
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    // ── BOOKINGS OPEN VIEW ───────────────────────────────────────────────
    // Shows the recruiter's full schedule. They can cancel bookings here.

    private JPanel buildBookingsOpenView() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CandidateUI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(CandidateUI.BG);
        top.add(CandidateUI.sectionLabel("Your Schedule — Bookings Open"), BorderLayout.WEST);
        top.add(CandidateUI.infoLabel("Candidates are booking now. You can cancel appointments if needed."),
                BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"Offer", "Start", "End", "Status", "Candidate"};
        scheduleModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = CandidateUI.styledTable(scheduleModel);
        panel.add(CandidateUI.scrolled(table), BorderLayout.CENTER);

        JButton refreshBtn = CandidateUI.accentButton("Refresh Schedule");
        refreshBtn.addActionListener(e -> refreshSchedule());

        JButton cancelBtn = CandidateUI.dangerButton("Cancel Selected Appointment");
        cancelBtn.addActionListener(e -> cancelAppointment(table));

        JButton notifsBtn = CandidateUI.accentButton("View Notifications");
        notifsBtn.addActionListener(e -> showNotificationsDialog());

        panel.add(CandidateUI.buttonRow(refreshBtn, cancelBtn, notifsBtn), BorderLayout.SOUTH);
        return panel;
    }

    private void cancelAppointment(JTable table) {
        if (recruiter == null) { CandidateUI.err(this, "Not logged in."); return; }
        int row = table.getSelectedRow();
        if (row < 0 || row >= scheduleSlotCache.size()) {
            CandidateUI.err(this, "Select an appointment to cancel.");
            return;
        }
        AppointmentSlot slot = scheduleSlotCache.get(row);
        Reservation res = slot.getReservation();
        if (res == null || res.getStatus() == ReservationStatus.CANCELLED) {
            CandidateUI.err(this, "This slot has no active booking to cancel.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel appointment with " + res.getCandidate().getName() + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Candidate affected = res.getCandidate();
        recruiter.cancelAppointment(res);

        // notify the candidate about the cancellation
        Notification notice = new Notification(
                UUID.randomUUID().toString(),
                "Your appointment for '" + slot.getOffer().getTitle()
                        + "' with " + recruiter.getName() + " at "
                        + slot.getStartTime().format(FMT) + " has been cancelled by the recruiter.",
                affected.getName());
        notice.send();
        affected.addNotification(notice);

        system.logEvent("Recruiter " + recruiter.getName()
                + " cancelled appointment with " + affected.getName(),
                recruiter.getName());

        refreshSchedule();
        CandidateUI.ok(this, "Appointment cancelled. The candidate has been notified.");
    }

    // ── BOOKINGS CLOSED VIEW ─────────────────────────────────────────────
    // Read-only schedule so the recruiter can prepare for the fair.

    private JPanel buildBookingsClosedView() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CandidateUI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(CandidateUI.BG);
        top.add(CandidateUI.sectionLabel("Your Schedule — Bookings Closed"), BorderLayout.WEST);
        top.add(CandidateUI.infoLabel("Bookings are closed. Review your schedule before the fair starts."),
                BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        // reuse the same table model — it gets refreshed on state change
        String[] cols = {"Offer", "Start", "End", "Status", "Candidate"};
        DefaultTableModel closedModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = CandidateUI.styledTable(closedModel);
        panel.add(CandidateUI.scrolled(table), BorderLayout.CENTER);

        JButton refreshBtn = CandidateUI.accentButton("Refresh Schedule");
        refreshBtn.addActionListener(e -> {
            closedModel.setRowCount(0);
            if (recruiter == null) return;
            for (AvailabilityBlock block : recruiter.getAvailabilityBlocks()) {
                for (AppointmentSlot slot : block.getSlots()) {
                    Reservation res = slot.getReservation();
                    String status = slot.isAvailable() ? "Open" : (res != null ? res.getStatus().name() : "Booked");
                    String candidateName = (res != null && res.getStatus() != ReservationStatus.CANCELLED)
                            ? res.getCandidate().getName() : "—";
                    closedModel.addRow(new Object[]{
                            slot.getOffer().getTitle(),
                            slot.getStartTime().format(FMT),
                            slot.getEndTime().format(FMT),
                            status,
                            candidateName
                    });
                }
            }
        });

        JButton notifsBtn = CandidateUI.accentButton("View Notifications");
        notifsBtn.addActionListener(e -> showNotificationsDialog());

        panel.add(CandidateUI.buttonRow(refreshBtn, notifsBtn), BorderLayout.SOUTH);
        return panel;
    }

    // ── FAIR LIVE VIEW ───────────────────────────────────────────────────
    // The recruiter hosts live sessions here. They see who they're meeting,
    // can end sessions early, and record attendance.

    private JPanel buildFairLiveView() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CandidateUI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(CandidateUI.BG);
        top.add(CandidateUI.sectionLabel("Live Fair — Session Hosting"), BorderLayout.WEST);
        panel.add(top, BorderLayout.NORTH);

        // centre area: session status display
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.Y_AXIS));
        centrePanel.setBackground(CandidateUI.BG);
        centrePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        sessionStatusLabel = new JLabel("No active session");
        sessionStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        sessionStatusLabel.setForeground(CandidateUI.PRIMARY);
        sessionStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lobbyCountLabel = new JLabel("Lobby: 0 waiting");
        lobbyCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lobbyCountLabel.setForeground(CandidateUI.MUTED);
        lobbyCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centrePanel.add(sessionStatusLabel);
        centrePanel.add(Box.createVerticalStrut(8));
        centrePanel.add(lobbyCountLabel);
        centrePanel.add(Box.createVerticalStrut(20));

        // session control buttons — shown when a session is active
        sessionControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        sessionControlPanel.setBackground(CandidateUI.BG);
        sessionControlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton endSessionBtn = CandidateUI.dangerButton("End Session Early");
        endSessionBtn.addActionListener(e -> endCurrentSession());

        JButton attendedBtn = CandidateUI.accentButton("Record: Attended");
        attendedBtn.addActionListener(e -> recordOutcome(AttendOutcome.ATTENDED));

        JButton noShowBtn = CandidateUI.dangerButton("Record: No Show");
        noShowBtn.addActionListener(e -> recordOutcome(AttendOutcome.NO_SHOW));

        JButton leftEarlyBtn = CandidateUI.dangerButton("Record: Left Early");
        leftEarlyBtn.addActionListener(e -> recordOutcome(AttendOutcome.LEFT_EARLY));

        sessionControlPanel.add(endSessionBtn);
        sessionControlPanel.add(attendedBtn);
        sessionControlPanel.add(noShowBtn);
        sessionControlPanel.add(leftEarlyBtn);

        centrePanel.add(sessionControlPanel);
        panel.add(centrePanel, BorderLayout.CENTER);

        // bottom: start next session + notifications
        JButton startNextBtn = CandidateUI.accentButton("Start Next Session");
        startNextBtn.addActionListener(e -> startNextScheduledSession());

        JButton notifsBtn = CandidateUI.accentButton("View Notifications");
        notifsBtn.addActionListener(e -> showNotificationsDialog());

        JButton refreshBtn = CandidateUI.accentButton("Refresh");
        refreshBtn.addActionListener(e -> refreshLiveView());

        panel.add(CandidateUI.buttonRow(startNextBtn, refreshBtn, notifsBtn), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Finds the next confirmed reservation whose time window matches
     * (or has passed) the current simulated time, and starts a session for it.
     */
    private void startNextScheduledSession() {
        if (recruiter == null) { CandidateUI.err(this, "Not logged in."); return; }
        if (currentSession != null && currentSession.isActive()) {
            CandidateUI.err(this, "A session is already active. End it first.");
            return;
        }

        LocalDateTime now = timer.getCurrentTime();

        // go through all slots and find the next confirmed one whose window has arrived
        Reservation nextRes = null;
        AppointmentSlot nextSlot = null;
        for (AvailabilityBlock block : recruiter.getAvailabilityBlocks()) {
            for (AppointmentSlot slot : block.getSlots()) {
                Reservation res = slot.getReservation();
                if (res == null) continue;
                if (res.getStatus() != ReservationStatus.CONFIRMED) continue;
                if (res.getAttendanceRecord() != null) continue; // already done
                if (!now.isBefore(slot.getStartTime())) {
                    // this slot's time has arrived or passed
                    if (nextSlot == null || slot.getStartTime().isBefore(nextSlot.getStartTime())) {
                        nextRes = res;
                        nextSlot = slot;
                    }
                }
            }
        }

        if (nextRes == null) {
            CandidateUI.err(this, "No upcoming sessions ready to start right now.");
            return;
        }

        // find or set up the virtual room for this booth
        findMyRoom();

        currentReservation = nextRes;
        currentSession = new MeetingSession(
                recruiter, nextRes.getCandidate(),
                nextSlot.getStartTime(), nextSlot.getEndTime());

        if (myRoom != null) {
            myRoom.setCurrentSession(currentSession);
        }
        currentSession.start();
        system.logEvent("Session started: " + recruiter.getName()
                + " with " + nextRes.getCandidate().getName(), recruiter.getName());
        refreshLiveView();
    }

    private void endCurrentSession() {
        if (currentSession == null || !currentSession.isActive()) {
            CandidateUI.err(this, "No active session to end.");
            return;
        }
        currentSession.end();
        system.logEvent("Session ended early by " + recruiter.getName(), recruiter.getName());
        refreshLiveView();
    }

    private void recordOutcome(AttendOutcome outcome) {
        if (currentSession == null) {
            CandidateUI.err(this, "No session to record attendance for.");
            return;
        }
        if (currentReservation == null) {
            CandidateUI.err(this, "No reservation linked to this session.");
            return;
        }

        // make sure the session is stopped
        if (currentSession.isActive()) {
            currentSession.end();
        }

        LocalDateTime joinTime = currentSession.getStartTime();
        LocalDateTime leaveTime = timer.getCurrentTime();

        if (outcome == AttendOutcome.NO_SHOW) {
            attendanceLogger.recordNoShow(currentReservation, joinTime);
        } else {
            attendanceLogger.recordAttendance(joinTime, leaveTime, outcome, currentReservation);
        }

        system.logEvent("Attendance recorded: " + outcome.name()
                + " for " + currentReservation.getCandidate().getName()
                + " by " + recruiter.getName(), recruiter.getName());

        // clear the current session so we can move to the next one
        if (myRoom != null) {
            myRoom.setCurrentSession(null);
        }
        currentSession = null;
        currentReservation = null;
        refreshLiveView();
        CandidateUI.ok(this, "Attendance recorded: " + outcome.name());
    }

    private void findMyRoom() {
        if (myRoom != null) return;
        if (fairLive == null) return;

        // if fairLive rooms haven't been set up yet, start the fair
        if (fairLive.getActiveRooms().isEmpty()
                && system.getState() == SystemState.FAIR_LIVE) {
            fairLive.startFair();
        }

        for (VirtualRoom room : fairLive.getActiveRooms()) {
            if (room.getBooth().equals(recruiterBooth)) {
                myRoom = room;
                return;
            }
        }
    }

    private void refreshLiveView() {
        findMyRoom();

        if (currentSession != null && currentSession.isActive()) {
            sessionStatusLabel.setText("In session with: " + currentSession.getCandidate().getName());
            sessionStatusLabel.setForeground(new Color(22, 163, 74)); // green
        } else if (currentSession != null && !currentSession.isActive()) {
            // session ended but attendance not yet recorded
            sessionStatusLabel.setText("Session ended — record attendance for: "
                    + currentSession.getCandidate().getName());
            sessionStatusLabel.setForeground(new Color(234, 88, 12)); // orange
        } else {
            sessionStatusLabel.setText("No active session — click 'Start Next Session'");
            sessionStatusLabel.setForeground(CandidateUI.PRIMARY);
        }

        if (myRoom != null) {
            int waiting = myRoom.getLobby().getWaitingCandidates().size();
            lobbyCountLabel.setText("Lobby: " + waiting + " candidate(s) waiting");
        } else {
            lobbyCountLabel.setText("Lobby: —");
        }
    }

    // ── SCHEDULE REFRESH ─────────────────────────────────────────────────

    private void refreshSchedule() {
        scheduleModel.setRowCount(0);
        scheduleSlotCache.clear();
        if (recruiter == null) return;
        for (AvailabilityBlock block : recruiter.getAvailabilityBlocks()) {
            for (AppointmentSlot slot : block.getSlots()) {
                Reservation res = slot.getReservation();
                String status;
                String candidateName;
                if (slot.isAvailable()) {
                    status = "Open";
                    candidateName = "—";
                } else if (res != null && res.getStatus() != ReservationStatus.CANCELLED) {
                    status = res.getStatus().name();
                    candidateName = res.getCandidate().getName();
                } else {
                    status = "Cancelled";
                    candidateName = "—";
                }
                scheduleModel.addRow(new Object[]{
                        slot.getOffer().getTitle(),
                        slot.getStartTime().format(FMT),
                        slot.getEndTime().format(FMT),
                        status,
                        candidateName
                });
                scheduleSlotCache.add(slot);
            }
        }
    }

    // ── NOTIFICATIONS DIALOG ─────────────────────────────────────────────

    private void showNotificationsDialog() {
        if (recruiter == null) { CandidateUI.err(this, "Not logged in."); return; }

        String[] cols = {"Timestamp", "Message"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Notification> notifs = recruiter.getNotifications();
        for (int i = notifs.size() - 1; i >= 0; i--) {
            Notification n = notifs.get(i);
            model.addRow(new Object[]{n.getTimestamp().format(FMT), n.getMessage()});
        }

        JTable table = CandidateUI.styledTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setPreferredWidth(500);
        JScrollPane scroll = CandidateUI.scrolled(table);
        scroll.setPreferredSize(new Dimension(660, 300));

        JOptionPane.showMessageDialog(this, scroll,
                "Notifications (" + notifs.size() + ")",
                JOptionPane.PLAIN_MESSAGE);
    }

    // ── STATE-BASED VIEW SWITCHING ───────────────────────────────────────

    private void showViewForCurrentState() {
        if (recruiter == null) {
            contentLayout.show(contentPanel, VIEW_LOGIN);
            return;
        }
        switch (system.getState()) {
            case PREPARING:
                contentLayout.show(contentPanel, VIEW_PREPARING);
                break;
            case BOOKINGS_OPEN:
                refreshSchedule();
                contentLayout.show(contentPanel, VIEW_BOOKINGS_OPEN);
                break;
            case BOOKINGS_CLOSED:
                contentLayout.show(contentPanel, VIEW_BOOKINGS_CLOSED);
                break;
            case FAIR_LIVE:
                findMyRoom();
                refreshLiveView();
                contentLayout.show(contentPanel, VIEW_FAIR_LIVE);
                break;
            default:
                contentLayout.show(contentPanel, VIEW_LOGIN);
                break;
        }
    }

    // ── TIMER LISTENER ───────────────────────────────────────────────────

    @Override
    public void onTimeChanged(LocalDateTime newTime) {
        SwingUtilities.invokeLater(() -> {
            timeLabel.setText("Time: " + newTime.format(FMT));
            stateLabel.setText("State: " + system.getState());

            // automatically switch views when the system state changes
            showViewForCurrentState();

            // during the live fair, check if the current session's time is up
            if (system.getState() == SystemState.FAIR_LIVE
                    && currentSession != null
                    && currentSession.isActive()) {
                if (!newTime.isBefore(currentSession.getEndTime())) {
                    currentSession.end();
                    system.logEvent("Session time expired for " + recruiter.getName(),
                            recruiter.getName());
                    refreshLiveView();
                }
            }
        });
    }
}
