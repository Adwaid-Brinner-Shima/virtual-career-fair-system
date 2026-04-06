package vcfs.ui;

import vcfs.model.*;
import vcfs.timer.SystemTimer;
import vcfs.timer.TimerListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Candidate-facing screen for the Virtual Career Fair System.
 *
 * Tab responsibilities are split across helper classes to keep this file lean:
 *   - "Register"   → CandidateProfile.buildPanel()
 *   - "Auto-Book"  → Request.buildPanel()
 *   - "Browse", "Reservations", "Notifications", "Lobby" → built here
 *
 * Uses RoomState to display live room status (CLOSED / OPEN / IN_SESSION).
 */
public class CandidateScreen extends JFrame implements TimerListener {

    private final VCFSystem   system;
    private final FairLive    fairLive;   // nullable — only available once fair is live
    private Candidate         candidate;  // set by CandidateProfile after registration

    private JLabel stateLabel;
    private JLabel timeLabel;

    // Live-refresh table models
    private DefaultTableModel slotsModel;
    private DefaultTableModel reservationsModel;
    private DefaultTableModel notificationsModel;
    private DefaultTableModel lobbyModel;
    private final List<AppointmentSlot> slotCache = new ArrayList<>();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ── Constructors ──────────────────────────────────────────────────────

    public CandidateScreen(VCFSystem system, SystemTimer timer, FairLive fairLive) {
        this.system   = system;
        this.fairLive = fairLive;
        timer.addListener(this);
        buildUI();
    }

    public CandidateScreen(VCFSystem system, SystemTimer timer) {
        this(system, timer, null);
    }

    // ── UI assembly ───────────────────────────────────────────────────────

    private void buildUI() {
        setTitle("VCFS — Candidate Portal");
        setSize(840, 660);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CandidateUI.BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(CandidateUI.PRIMARY);
        h.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("Candidate Portal");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(CandidateUI.WHITE);
        JPanel right = new JPanel(new GridLayout(2, 1));
        right.setBackground(CandidateUI.PRIMARY);
        timeLabel  = new JLabel("Time: --", SwingConstants.RIGHT);
        stateLabel = new JLabel("State: " + system.getState(), SwingConstants.RIGHT);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(148, 163, 184));
        stateLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        stateLabel.setForeground(new Color(134, 239, 172));
        right.add(timeLabel); right.add(stateLabel);
        h.add(title, BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    /**
     * Assembles tabs — Register and Auto-Book panels are fully delegated
     * to CandidateProfile and Request respectively.
     */
    private JTabbedPane buildTabs() {
        CandidateProfile profile = new CandidateProfile();
        Request          request = new Request();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(CandidateUI.BG);
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tabs.addTab("👤  Register",       profile.buildPanel(system, c -> this.candidate = c));
        tabs.addTab("🔍  Browse",         buildBrowseTab());
        tabs.addTab("🤖  Auto-Book",      request.buildPanel(system, () -> candidate));
        tabs.addTab("📅  Reservations",   buildReservationsTab());
        tabs.addTab("🔔  Notifications",  buildNotificationsTab());
        tabs.addTab("🏠  Lobby",          buildLobbyTab());

        return tabs;
    }

    // ── Tab: Browse & Manual Book ─────────────────────────────────────────

    private JPanel buildBrowseTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CandidateUI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        String[] cols = {"Organisation", "Booth", "Offer", "Recruiter", "Start", "End", "Free"};
        slotsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = CandidateUI.styledTable(slotsModel);
        panel.add(CandidateUI.scrolled(table), BorderLayout.CENTER);

        JButton refreshBtn = CandidateUI.accentButton("↻  Refresh");
        refreshBtn.addActionListener(e -> refreshSlots());

        JButton bookBtn = CandidateUI.accentButton("✔  Book Selected");
        bookBtn.addActionListener(e -> manualBook(table));

        panel.add(CandidateUI.buttonRow(refreshBtn, bookBtn), BorderLayout.SOUTH);
        refreshSlots();
        return panel;
    }

    private void manualBook(JTable table) {
        if (candidate == null)                                      { CandidateUI.err(this, "Please register first."); return; }
        if (system.getState() != SystemState.BOOKINGS_OPEN)        { CandidateUI.err(this, "Bookings are not currently open."); return; }
        int row = table.getSelectedRow();
        if (row < 0 || row >= slotCache.size())                    { CandidateUI.err(this, "Select a slot first."); return; }
        AppointmentSlot slot = slotCache.get(row);
        if (!slot.isAvailable())                                    { CandidateUI.err(this, "Slot no longer available."); return; }
        Reservation res = slot.book(candidate);
        if (res == null)                                            { CandidateUI.err(this, "Booking failed."); return; }
        res.confirm();
        candidate.addReservation(res);
        system.logEvent("Reserved: " + slot.getOffer().getTitle() + " by " + candidate.getName(), candidate.getName());
        refreshSlots();
        CandidateUI.ok(this, "Slot booked!");
    }

    // ── Tab: My Reservations ──────────────────────────────────────────────

    private JPanel buildReservationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CandidateUI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        String[] cols = {"ID", "Offer", "Recruiter", "Start", "End", "Status", "Attendance"};
        reservationsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = CandidateUI.styledTable(reservationsModel);
        panel.add(CandidateUI.scrolled(table), BorderLayout.CENTER);

        JButton refreshBtn = CandidateUI.accentButton("↻  Refresh");
        refreshBtn.addActionListener(e -> refreshReservations());

        JButton cancelBtn = CandidateUI.dangerButton("✖  Cancel Selected");
        cancelBtn.addActionListener(e -> cancelSelected(table));

        panel.add(CandidateUI.buttonRow(refreshBtn, cancelBtn), BorderLayout.SOUTH);
        return panel;
    }

    private void cancelSelected(JTable table) {
        if (candidate == null)                               { CandidateUI.err(this, "No candidate registered."); return; }
        if (system.getState() != SystemState.BOOKINGS_OPEN) { CandidateUI.err(this, "Cancellations only allowed during BOOKINGS_OPEN."); return; }
        int row = table.getSelectedRow();
        if (row < 0)                                         { CandidateUI.err(this, "Select a reservation to cancel."); return; }
        List<Reservation> active = activeReservations();
        if (row >= active.size()) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel this reservation?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        candidate.cancelReservation(active.get(row));
        system.logEvent("Reservation cancelled by " + candidate.getName(), candidate.getName());
        refreshReservations(); refreshSlots();
        CandidateUI.ok(this, "Reservation cancelled.");
    }

    // ── Tab: Notifications ────────────────────────────────────────────────

    private JPanel buildNotificationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CandidateUI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        String[] cols = {"Timestamp", "Message"};
        notificationsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = CandidateUI.styledTable(notificationsModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(520);
        panel.add(CandidateUI.scrolled(table), BorderLayout.CENTER);

        JButton refreshBtn = CandidateUI.accentButton("↻  Refresh");
        refreshBtn.addActionListener(e -> refreshNotifications());
        panel.add(CandidateUI.buttonRow(refreshBtn), BorderLayout.SOUTH);
        return panel;
    }

    // ── Tab: Lobby & Live Session ─────────────────────────────────────────

    private JPanel buildLobbyTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CandidateUI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel hint = CandidateUI.infoLabel(
            "Shows all booth room states. Join a lobby when the fair is LIVE and your slot is upcoming.");
        panel.add(hint, BorderLayout.NORTH);

        String[] cols = {"Organisation", "Booth", "Room State", "Queue"};
        lobbyModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = CandidateUI.styledTable(lobbyModel);
        panel.add(CandidateUI.scrolled(table), BorderLayout.CENTER);

        JButton refreshBtn = CandidateUI.accentButton("↻  Refresh");
        refreshBtn.addActionListener(e -> refreshLobby());

        JButton joinBtn = CandidateUI.accentButton("🚪  Join Lobby");
        joinBtn.addActionListener(e -> joinLobby(table));

        panel.add(CandidateUI.buttonRow(refreshBtn, joinBtn), BorderLayout.SOUTH);
        refreshLobby();
        return panel;
    }

    private void joinLobby(JTable table) {
        if (candidate == null)                               { CandidateUI.err(this, "Please register first."); return; }
        if (system.getState() != SystemState.FAIR_LIVE)     { CandidateUI.err(this, "The fair is not live yet."); return; }
        if (fairLive == null)                                { CandidateUI.err(this, "Fair session unavailable."); return; }
        int row = table.getSelectedRow();
        if (row < 0)                                         { CandidateUI.err(this, "Select a booth to join."); return; }
        List<VirtualRoom> rooms = fairLive.getActiveRooms();
        if (row >= rooms.size()) return;
        VirtualRoom room = rooms.get(row);
        fairLive.sendToLobby(candidate, room);
        refreshLobby();
        CandidateUI.ok(this, "You are now in the lobby for: " + room.getBooth().getName());
    }

    // ── Refresh helpers ───────────────────────────────────────────────────

    private void refreshSlots() {
        slotsModel.setRowCount(0); slotCache.clear();
        for (Organisation org : system.getOrganisations())
            for (Booth booth : org.getBooths())
                for (Recruiter rec : booth.getRecruiters())
                    for (AvailabilityBlock blk : rec.getAvailabilityBlocks())
                        for (AppointmentSlot slot : blk.getSlots()) {
                            slotsModel.addRow(new Object[]{ org.getName(), booth.getName(),
                                slot.getOffer().getTitle(), rec.getName(),
                                slot.getStartTime().format(FMT), slot.getEndTime().format(FMT),
                                slot.isAvailable() ? "Yes" : "No" });
                            slotCache.add(slot);
                        }
    }

    private void refreshReservations() {
        reservationsModel.setRowCount(0);
        for (Reservation res : activeReservations()) {
            AppointmentSlot slot = res.getSlot();
            AttendanceRecord ar  = res.getAttendanceRecord();
            reservationsModel.addRow(new Object[]{ res.getId().substring(0, 8),
                slot.getOffer().getTitle(), slot.getRecruiter().getName(),
                slot.getStartTime().format(FMT), slot.getEndTime().format(FMT),
                res.getStatus(), ar != null ? ar.getOutcome() : "—" });
        }
    }

    private void refreshNotifications() {
        notificationsModel.setRowCount(0);
        if (candidate == null) return;
        List<Notification> ns = candidate.getNotifications();
        for (int i = ns.size() - 1; i >= 0; i--)
            notificationsModel.addRow(new Object[]{ ns.get(i).getTimestamp().format(FMT), ns.get(i).getMessage() });
    }

    private void refreshLobby() {
        lobbyModel.setRowCount(0);
        boolean isFairLive = fairLive != null && system.getState() == SystemState.FAIR_LIVE;
        if (isFairLive) {
            for (VirtualRoom room : fairLive.getActiveRooms()) {
                RoomState rs = deriveRoomState(room);
                int queue    = room.getLobby().getWaitingCandidates().size();
                lobbyModel.addRow(new Object[]{ orgNameFor(room.getBooth()), room.getBooth().getName(),
                    rs.name(), queue + " waiting" });
            }
        } else {
            for (Organisation org : system.getOrganisations())
                for (Booth booth : org.getBooths()) {
                    RoomState rs = booth.isRoomOpen() ? RoomState.OPEN : RoomState.CLOSED;
                    lobbyModel.addRow(new Object[]{ org.getName(), booth.getName(), rs.name(), "—" });
                }
        }
    }

    /** Derives a RoomState from a live VirtualRoom's runtime status. */
    private RoomState deriveRoomState(VirtualRoom room) {
        if (!room.isOpen()) return RoomState.CLOSED;
        MeetingSession s = room.getCurrentSession();
        return (s != null && s.isActive()) ? RoomState.IN_SESSION : RoomState.OPEN;
    }

    /** Looks up the Organisation name that owns the given Booth. */
    private String orgNameFor(Booth target) {
        for (Organisation org : system.getOrganisations())
            for (Booth b : org.getBooths())
                if (b.equals(target)) return org.getName();
        return "—";
    }

    /** Returns only PENDING / CONFIRMED reservations for the current candidate. */
    private List<Reservation> activeReservations() {
        List<Reservation> result = new ArrayList<>();
        if (candidate == null) return result;
        for (Reservation r : candidate.getReservations())
            if (r.getStatus() != ReservationStatus.CANCELLED) result.add(r);
        return result;
    }

    // ── TimerListener ─────────────────────────────────────────────────────

    @Override
    public void onTimeChanged(LocalDateTime newTime) {
        SwingUtilities.invokeLater(() -> {
            timeLabel.setText("Time: " + newTime.format(FMT));
            stateLabel.setText("State: " + system.getState());
            refreshSlots(); refreshReservations();
            refreshNotifications(); refreshLobby();
        });
    }
}
