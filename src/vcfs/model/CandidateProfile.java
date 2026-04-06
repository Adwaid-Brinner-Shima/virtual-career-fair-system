package vcfs.model;

import vcfs.model.Candidate;
import vcfs.model.VCFSystem;
import vcfs.ui.CandidateUI;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Holds a candidate's profile data (CV summary, interest tags) and
 * builds the Register / Profile tab panel for CandidateScreen.
 *
 * Extends User so the UI layer has a typed handle on the logged-in person.
 */
public class CandidateProfile extends User {

    private String    cvSummary    = "";
    private String    interestTags = "";
    private Candidate modelCandidate;          // set after successful registration

    public CandidateProfile() {
        super("", "", "");
    }

    // ── Data methods ──────────────────────────────────────────────────────

    /** Updates CV summary and interest tags used by the auto-booking engine. */
    public void updateProfile(String cvSummary, String interestTags) {
        this.cvSummary    = cvSummary;
        this.interestTags = interestTags;
    }

    public Candidate getModelCandidate() { return modelCandidate; }
    public String    getCvSummary()      { return cvSummary; }
    public String    getInterestTags()   { return interestTags; }

    // ── Panel builder ─────────────────────────────────────────────────────

    /**
     * Builds the Register + Profile tab panel.
     *
     * @param system       VCFSystem to register the new candidate into
     * @param onRegistered callback invoked with the created Candidate on success
     */
    public JPanel buildPanel(VCFSystem system, Consumer<Candidate> onRegistered) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CandidateUI.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ── Registration section ──────────────────────────────────────────
        panel.add(CandidateUI.sectionLabel("Register as a Candidate"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(CandidateUI.infoLabel("Fill in your details and click Register to join the fair."));
        panel.add(Box.createVerticalStrut(14));

        JTextField nameField  = CandidateUI.styledField("");
        JTextField emailField = CandidateUI.styledField("");
        panel.add(CandidateUI.labelledField("Full Name", nameField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(CandidateUI.labelledField("Email",     emailField));
        panel.add(Box.createVerticalStrut(12));

        JLabel statusLbl = CandidateUI.infoLabel("Not registered yet.");
        panel.add(statusLbl);
        panel.add(Box.createVerticalStrut(10));

        JButton registerBtn = CandidateUI.accentButton("Register");
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            if (name.isEmpty() || email.isEmpty()) {
                CandidateUI.err(panel, "Please enter both name and email."); return;
            }
            if (modelCandidate != null) {
                CandidateUI.err(panel, "Already registered as: " + modelCandidate.getName()); return;
            }
            this.id          = UUID.randomUUID().toString();
            this.displayName = name;
            this.email       = email;
            modelCandidate   = new Candidate(this.id, name, email);
            system.registerCandidate(modelCandidate);
            statusLbl.setText("✔  Registered: " + name + "  (" + email + ")");
            onRegistered.accept(modelCandidate);
        });
        panel.add(registerBtn);

        // ── Profile / interests section ───────────────────────────────────
        panel.add(Box.createVerticalStrut(24));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(16));
        panel.add(CandidateUI.sectionLabel("Profile & Interests"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(CandidateUI.infoLabel("Used by Auto-Book to match your interests to available offers."));
        panel.add(Box.createVerticalStrut(12));

        JTextField cvField   = CandidateUI.styledField(cvSummary);
        JTextField tagsField = CandidateUI.styledField(interestTags);
        panel.add(CandidateUI.labelledField("CV Summary",    cvField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(CandidateUI.labelledField("Interest Tags", tagsField));
        panel.add(Box.createVerticalStrut(14));

        JButton saveBtn = CandidateUI.accentButton("Save Profile");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            updateProfile(cvField.getText().trim(), tagsField.getText().trim());
            CandidateUI.ok(panel, "Profile saved.");
        });
        panel.add(saveBtn);

        return panel;
    }
}
