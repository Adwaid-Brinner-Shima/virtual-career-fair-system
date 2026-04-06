package vcfs.model;

import vcfs.model.*;
import vcfs.ui.CandidateUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Holds a candidate's booking preferences and implements the auto-booking engine.
 * Also builds the Auto-Book tab panel for CandidateScreen.
 */
public class Request {

    private Candidate requester;
    private String    desiredTags     = "";
    private String    preferredOrgs   = "";
    private int       maxAppointments = 3;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public Request() {}

    // ── Preference management ─────────────────────────────────────────────

    /** Updates booking preferences used when proposing auto-bookings. */
    public void updatePreferences(String desiredTags, String preferredOrgs, int maxAppointments) {
        this.desiredTags      = desiredTags;
        this.preferredOrgs    = preferredOrgs;
        this.maxAppointments  = maxAppointments;
    }

    /**
     * Scans all available slots and returns up to maxAppointments that
     * match desiredTags (against offer title/tags) and preferredOrgs.
     * Blank fields match everything.
     */
    public List<AppointmentSlot> proposeBookings(VCFSystem system) {
        List<AppointmentSlot> proposals = new ArrayList<>();
        String[] tagArr = split(desiredTags);
        String[] orgArr = split(preferredOrgs);

        outer:
        for (Organisation org : system.getOrganisations()) {
            if (orgArr.length > 0 && !anyMatch(org.getName().toLowerCase(), orgArr)) continue;
            for (Booth booth : org.getBooths())
                for (Recruiter rec : booth.getRecruiters())
                    for (AvailabilityBlock blk : rec.getAvailabilityBlocks())
                        for (AppointmentSlot slot : blk.getSlots()) {
                            if (!slot.isAvailable()) continue;
                            if (tagArr.length == 0 || offerMatchesTags(slot.getOffer(), tagArr)) {
                                proposals.add(slot);
                                if (proposals.size() >= maxAppointments) break outer;
                            }
                        }
        }
        return proposals;
    }

    // ── Panel builder ─────────────────────────────────────────────────────

    /**
     * Builds the Auto-Book tab panel.
     *
     * @param system           the VCFSystem to search for slots
     * @param candidateSupplier supplies the currently registered Candidate (may return null)
     */
    public JPanel buildPanel(VCFSystem system, Supplier<Candidate> candidateSupplier) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CandidateUI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // ── Preferences form ──────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CandidateUI.BG);
        form.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        form.add(CandidateUI.sectionLabel("Auto-Book Preferences"));
        form.add(Box.createVerticalStrut(6));
        form.add(CandidateUI.infoLabel("Comma-separated values — leave blank to match everything."));
        form.add(Box.createVerticalStrut(12));

        JTextField tagsField = CandidateUI.styledField(desiredTags);
        JTextField orgsField = CandidateUI.styledField(preferredOrgs);
        JTextField maxField  = CandidateUI.styledField(String.valueOf(maxAppointments));
        form.add(CandidateUI.labelledField("Desired Tags",     tagsField));
        form.add(Box.createVerticalStrut(8));
        form.add(CandidateUI.labelledField("Preferred Orgs",   orgsField));
        form.add(Box.createVerticalStrut(8));
        form.add(CandidateUI.labelledField("Max Appointments", maxField));
        panel.add(form, BorderLayout.NORTH);

        // ── Proposals table ───────────────────────────────────────────────
        String[] cols = {"Organisation", "Offer", "Recruiter", "Start", "End"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = CandidateUI.styledTable(tableModel);
        List<AppointmentSlot> proposalCache = new ArrayList<>();
        panel.add(CandidateUI.scrolled(table), BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────
        JButton proposeBtn = CandidateUI.accentButton("🔎  Propose Bookings");
        proposeBtn.addActionListener(e -> {
            Candidate c = candidateSupplier.get();
            if (c == null)                                     { CandidateUI.err(panel, "Please register first."); return; }
            if (system.getState() != SystemState.BOOKINGS_OPEN){ CandidateUI.err(panel, "Bookings are not currently open."); return; }
            try { updatePreferences(tagsField.getText().trim(), orgsField.getText().trim(),
                                    Integer.parseInt(maxField.getText().trim())); }
            catch (NumberFormatException ex)                   { CandidateUI.err(panel, "Max Appointments must be a whole number."); return; }
            requester = c;
            proposalCache.clear();
            proposalCache.addAll(proposeBookings(system));
            tableModel.setRowCount(0);
            for (AppointmentSlot slot : proposalCache)
                tableModel.addRow(new Object[]{ orgName(system, slot),
                    slot.getOffer().getTitle(), slot.getRecruiter().getName(),
                    slot.getStartTime().format(FMT), slot.getEndTime().format(FMT) });
            if (proposalCache.isEmpty()) CandidateUI.ok(panel, "No matching slots found.");
        });

        JButton acceptBtn = CandidateUI.accentButton("✔  Accept Selected");
        acceptBtn.addActionListener(e -> {
            Candidate c = candidateSupplier.get();
            if (c == null) { CandidateUI.err(panel, "Please register first."); return; }
            int row = table.getSelectedRow();
            if (row < 0 || row >= proposalCache.size()) { CandidateUI.err(panel, "Select a proposed slot."); return; }
            AppointmentSlot slot = proposalCache.get(row);
            Reservation res = slot.book(c);
            if (res == null) { CandidateUI.err(panel, "Slot is no longer available."); return; }
            res.confirm();
            c.addReservation(res);
            system.logEvent("Auto-booked: " + slot.getOffer().getTitle() + " for " + c.getName(), c.getName());
            proposalCache.remove(row);
            tableModel.removeRow(row);
            CandidateUI.ok(panel, "✔ Slot booked successfully!");
        });

        JButton rejectBtn = CandidateUI.dangerButton("✖  Reject Selected");
        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= proposalCache.size()) { CandidateUI.err(panel, "Select a slot to reject."); return; }
            proposalCache.remove(row);
            tableModel.removeRow(row);
        });

        panel.add(CandidateUI.buttonRow(proposeBtn, acceptBtn, rejectBtn), BorderLayout.SOUTH);
        return panel;
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private boolean offerMatchesTags(Offer offer, String[] tagArr) {
        if (anyMatch(offer.getTitle().toLowerCase(), tagArr)) return true;
        for (String tag : offer.getTags())
            if (anyMatch(tag.toLowerCase(), tagArr)) return true;
        return false;
    }

    private boolean anyMatch(String value, String[] terms) {
        for (String t : terms) if (value.contains(t.trim())) return true;
        return false;
    }

    private String[] split(String csv) {
        return csv == null || csv.isBlank() ? new String[0] : csv.toLowerCase().split(",");
    }

    private String orgName(VCFSystem system, AppointmentSlot slot) {
        for (Organisation org : system.getOrganisations())
            for (Booth b : org.getBooths())
                for (Recruiter r : b.getRecruiters())
                    if (r.equals(slot.getRecruiter())) return org.getName();
        return "—";
    }
}