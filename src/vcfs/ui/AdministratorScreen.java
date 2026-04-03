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
import java.util.List;
import java.util.UUID;

/**
 * The Administrator screen for the Virtual Career Fair System.
 * Allows the administrator to:
 * - Configure system milestone times
 * - Add/update organisations, booths, and recruiters
 * - Reset fair data
 * - View audit log entries
 */
public class AdministratorScreen extends JFrame implements TimerListener {

    private VCFSystem system;
    private SystemTimer timer;

    // Display
    private JLabel stateLabel;
    private JLabel timeLabel;

    // Tables
    private DefaultTableModel orgTableModel;
    private DefaultTableModel boothTableModel;
    private DefaultTableModel recruiterTableModel;
    private DefaultTableModel auditTableModel;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static final Color PRIMARY   = new Color(30, 41, 59);
    private static final Color ACCENT    = new Color(59, 130, 246);
    private static final Color BG        = new Color(248, 250, 252);
    private static final Color WHITE     = Color.WHITE;
    private static final Color BORDER    = new Color(226, 232, 240);
    private static final Color MUTED     = new Color(100, 116, 139);
    private static final Color DANGER    = new Color(239, 68, 68);

    public AdministratorScreen(VCFSystem system, SystemTimer timer) {
        this.system = system;
        this.timer = timer;
        timer.addListener(this);
        buildUI();
    }

    private void buildUI() {
        setTitle("VCFS — Administrator");
        setSize(820, 640);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Root layout
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);

        setContentPane(root);
    }

    // ─────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Administrator Panel");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(WHITE);

        JPanel rightInfo = new JPanel(new GridLayout(2, 1));
        rightInfo.setBackground(PRIMARY);

        timeLabel = new JLabel("Time: --");
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(148, 163, 184));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        stateLabel = new JLabel("State: " + system.getState());
        stateLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        stateLabel.setForeground(new Color(134, 239, 172));
        stateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        rightInfo.add(timeLabel);
        rightInfo.add(stateLabel);

        header.add(title, BorderLayout.WEST);
        header.add(rightInfo, BorderLayout.EAST);
        return header;
    }

    // ─────────────────────────────────────────────
    // TABS
    // ─────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tabs.addTab("⚙  System Config", buildConfigTab());
        tabs.addTab("🏢  Organisations", buildOrganisationsTab());
        tabs.addTab("🪑  Booths", buildBoothsTab());
        tabs.addTab("👤  Recruiters", buildRecruitersTab());
        tabs.addTab("📋  Audit Log", buildAuditTab());

        return tabs;
    }

    // ─────────────────────────────────────────────
    // TAB 1: SYSTEM CONFIG
    // ─────────────────────────────────────────────
    private JPanel buildConfigTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        panel.add(sectionLabel("Configure Fair Timeline"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(infoLabel("Format: dd-MM-yyyy HH:mm  (e.g. 01-04-2026 09:00)"));
        panel.add(Box.createVerticalStrut(16));

        JTextField bookingsOpenField  = styledField(formatOrEmpty(system.getBookingsOpenTime()));
        JTextField bookingsCloseField = styledField(formatOrEmpty(system.getBookingsCloseTime()));
        JTextField startField         = styledField(formatOrEmpty(system.getStartTime()));
        JTextField endField           = styledField(formatOrEmpty(system.getEndTime()));

        panel.add(labelledField("Bookings Open Time",  bookingsOpenField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(labelledField("Bookings Close Time", bookingsCloseField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(labelledField("Fair Start Time",     startField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(labelledField("Fair End Time",       endField));
        panel.add(Box.createVerticalStrut(20));

        JButton saveBtn = accentButton("Save Configuration");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            try {
                system.setBookingsOpenTime(LocalDateTime.parse(bookingsOpenField.getText().trim(), FORMATTER));
                system.setBookingsCloseTime(LocalDateTime.parse(bookingsCloseField.getText().trim(), FORMATTER));
                system.setStartTime(LocalDateTime.parse(startField.getText().trim(), FORMATTER));
                system.setEndTime(LocalDateTime.parse(endField.getText().trim(), FORMATTER));
                system.logEvent("Administrator updated system configuration", "Administrator");
                showSuccess("Configuration saved successfully!");
            } catch (DateTimeParseException ex) {
                showError("Invalid date format. Please use dd-MM-yyyy HH:mm");
            }
        });

        panel.add(saveBtn);
        panel.add(Box.createVerticalStrut(30));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(20));

        panel.add(sectionLabel("Reset Fair Data"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(infoLabel("This will delete all reservations, sessions, candidates, and notifications."));
        panel.add(Box.createVerticalStrut(12));

        JButton resetBtn = dangerButton("Reset All Fair Data");
        resetBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure? This will permanently delete all fair data.",
                "Confirm Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                system.resetData();
                refreshAuditTable();
                showSuccess("All fair data has been reset.");
            }
        });

        panel.add(resetBtn);
        return panel;
    }

    // ─────────────────────────────────────────────
    // TAB 2: ORGANISATIONS
    // ─────────────────────────────────────────────
    private JPanel buildOrganisationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Table
        String[] cols = {"ID", "Name", "Description", "Booths"};
        orgTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(orgTableModel);
        refreshOrgTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        panel.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(BG);

        JButton addBtn = accentButton("+ Add Organisation");
        JButton editBtn = accentButton("Edit");
        JButton deleteBtn = dangerButton("Delete");

        addBtn.addActionListener(e -> {
            JTextField nameField = styledField("");
            JTextField descField = styledField("");
            Object[] fields = {"Name:", nameField, "Description:", descField};
            int result = JOptionPane.showConfirmDialog(this, fields,
                "Add Organisation", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String desc = descField.getText().trim();
                if (!name.isEmpty()) {
                    Organisation org = new Organisation(UUID.randomUUID().toString(), name, desc);
                    system.addOrganisation(org);
                    system.logEvent("Organisation added: " + name, "Administrator");
                    refreshOrgTable();
                    refreshAuditTable();
                }
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showError("Please select an organisation to edit."); return; }
            Organisation org = system.getOrganisations().get(row);
            JTextField nameField = styledField(org.getName());
            JTextField descField = styledField(org.getDescription());
            Object[] fields = {"Name:", nameField, "Description:", descField};
            int result = JOptionPane.showConfirmDialog(this, fields,
                "Edit Organisation", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                org.setName(nameField.getText().trim());
                org.setDescription(descField.getText().trim());
                system.logEvent("Organisation updated: " + org.getName(), "Administrator");
                refreshOrgTable();
                refreshAuditTable();
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showError("Please select an organisation to delete."); return; }
            Organisation org = system.getOrganisations().get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete organisation: " + org.getName() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                system.getOrganisations().remove(row);
                system.logEvent("Organisation deleted: " + org.getName(), "Administrator");
                refreshOrgTable();
                refreshAuditTable();
            }
        });

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ─────────────────────────────────────────────
    // TAB 3: BOOTHS
    // ─────────────────────────────────────────────
    private JPanel buildBoothsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        String[] cols = {"ID", "Name", "Organisation", "Room Open"};
        boothTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(boothTableModel);
        refreshBoothTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(BG);

        JButton addBtn = accentButton("+ Add Booth");

        addBtn.addActionListener(e -> {
            List<Organisation> orgs = system.getOrganisations();
            if (orgs.isEmpty()) {
                showError("Please add an organisation first.");
                return;
            }
            String[] orgNames = orgs.stream().map(Organisation::getName).toArray(String[]::new);
            JTextField nameField = styledField("");
            JComboBox<String> orgCombo = new JComboBox<>(orgNames);
            Object[] fields = {"Booth Name:", nameField, "Organisation:", orgCombo};
            int result = JOptionPane.showConfirmDialog(this, fields,
                "Add Booth", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    Booth booth = new Booth(UUID.randomUUID().toString(), name);
                    orgs.get(orgCombo.getSelectedIndex()).addBooth(booth);
                    system.logEvent("Booth added: " + name, "Administrator");
                    refreshBoothTable();
                    refreshAuditTable();
                }
            }
        });

        btnPanel.add(addBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ─────────────────────────────────────────────
    // TAB 4: RECRUITERS
    // ─────────────────────────────────────────────
    private JPanel buildRecruitersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        String[] cols = {"ID", "Name", "Email", "Booth"};
        recruiterTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(recruiterTableModel);
        refreshRecruiterTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(BG);

        JButton addBtn = accentButton("+ Add Recruiter");

        addBtn.addActionListener(e -> {
            // Collect all booths across all orgs
            java.util.List<Booth> allBooths = new java.util.ArrayList<>();
            for (Organisation org : system.getOrganisations()) {
                allBooths.addAll(org.getBooths());
            }
            if (allBooths.isEmpty()) {
                showError("Please add a booth first.");
                return;
            }
            String[] boothNames = allBooths.stream().map(Booth::getName).toArray(String[]::new);
            JTextField nameField = styledField("");
            JTextField emailField = styledField("");
            JComboBox<String> boothCombo = new JComboBox<>(boothNames);
            Object[] fields = {"Name:", nameField, "Email:", emailField, "Booth:", boothCombo};
            int result = JOptionPane.showConfirmDialog(this, fields,
                "Add Recruiter", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                if (!name.isEmpty() && !email.isEmpty()) {
                    Recruiter recruiter = new Recruiter(UUID.randomUUID().toString(), name, email);
                    allBooths.get(boothCombo.getSelectedIndex()).addRecruiter(recruiter);
                    system.logEvent("Recruiter added: " + name, "Administrator");
                    refreshRecruiterTable();
                    refreshAuditTable();
                }
            }
        });

        btnPanel.add(addBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ─────────────────────────────────────────────
    // TAB 5: AUDIT LOG
    // ─────────────────────────────────────────────
    private JPanel buildAuditTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        String[] cols = {"Timestamp", "Actor", "Event"};
        auditTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(auditTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(400);
        refreshAuditTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        panel.add(scroll, BorderLayout.CENTER);

        JButton refreshBtn = accentButton("↻  Refresh");
        refreshBtn.addActionListener(e -> refreshAuditTable());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(BG);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────
    // TABLE REFRESH HELPERS
    // ─────────────────────────────────────────────
    private void refreshOrgTable() {
        orgTableModel.setRowCount(0);
        for (Organisation org : system.getOrganisations()) {
            orgTableModel.addRow(new Object[]{
                org.getId().substring(0, 8),
                org.getName(),
                org.getDescription(),
                org.getBooths().size()
            });
        }
    }

    private void refreshBoothTable() {
        boothTableModel.setRowCount(0);
        for (Organisation org : system.getOrganisations()) {
            for (Booth booth : org.getBooths()) {
                boothTableModel.addRow(new Object[]{
                    booth.getId().substring(0, 8),
                    booth.getName(),
                    org.getName(),
                    booth.isRoomOpen() ? "Yes" : "No"
                });
            }
        }
    }

    private void refreshRecruiterTable() {
        recruiterTableModel.setRowCount(0);
        for (Organisation org : system.getOrganisations()) {
            for (Booth booth : org.getBooths()) {
                for (Recruiter rec : booth.getRecruiters()) {
                    recruiterTableModel.addRow(new Object[]{
                        rec.getId().substring(0, 8),
                        rec.getName(),
                        rec.getEmail(),
                        booth.getName()
                    });
                }
            }
        }
    }

    private void refreshAuditTable() {
        auditTableModel.setRowCount(0);
        List<AuditLog> logs = system.getAuditLogs();
        // Show newest first
        for (int i = logs.size() - 1; i >= 0; i--) {
            AuditLog log = logs.get(i);
            auditTableModel.addRow(new Object[]{
                log.getTimestamp().format(FORMATTER),
                log.getActor(),
                log.getEvent()
            });
        }
    }

    // ─────────────────────────────────────────────
    // UI COMPONENT HELPERS
    // ─────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel infoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.ITALIC, 11));
        label.setForeground(MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField styledField(String value) {
        JTextField field = new JTextField(value, 20);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    private JPanel labelledField(String labelText, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(500, 40));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(PRIMARY);
        label.setPreferredSize(new Dimension(160, 30));
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JButton accentButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setBackground(ACCENT);
        btn.setForeground(WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setBackground(DANGER);
        btn.setForeground(WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(PRIMARY);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(PRIMARY);
        table.setShowVerticalLines(false);
        return table;
    }

    private String formatOrEmpty(LocalDateTime dt) {
        return dt == null ? "" : dt.format(FORMATTER);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ─────────────────────────────────────────────
    // TIMER LISTENER
    // ─────────────────────────────────────────────
    @Override
    public void onTimeChanged(LocalDateTime newTime) {
        SwingUtilities.invokeLater(() -> {
            timeLabel.setText("Time: " + newTime.format(FORMATTER));
            stateLabel.setText("State: " + system.getState());
            refreshAuditTable();
        });
    }
}
