package vcfs.ui;

import vcfs.model.VCFSystem;
import vcfs.timer.SystemTimer;
import vcfs.timer.TimerListener;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The SystemTimer screen - a separate UI window that acts as the engine
 * driving state changes across the entire VCFS application.
 *
 * Required controls per specification:
 * - Skip 5 minutes (micro-skip)
 * - Skip 1 day (macro-skip)
 * - Jump directly to milestone times
 * - Display current simulated time and system state
 */
public class SystemTimerScreen extends JFrame implements TimerListener {

    private SystemTimer timer;
    private VCFSystem system;

    // Display labels
    private JLabel timeLabel;
    private JLabel stateLabel;
    private JLabel stateBadge;

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");

    // State badge colors
    private static final Color DORMANT_COLOR      = new Color(100, 116, 139);
    private static final Color PREPARING_COLOR    = new Color(234, 179, 8);
    private static final Color BOOKINGS_OPEN_COLOR  = new Color(34, 197, 94);
    private static final Color BOOKINGS_CLOSED_COLOR = new Color(249, 115, 22);
    private static final Color FAIR_LIVE_COLOR    = new Color(239, 68, 68);

    public SystemTimerScreen(SystemTimer timer, VCFSystem system) {
        this.timer = timer;
        this.system = system;
        timer.addListener(this);
        buildUI();
    }

    private void buildUI() {
        setTitle("VCFS — System Timer");
        setSize(420, 380);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel();
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        root.setBackground(new Color(248, 250, 252));

        // --- TOP: Clock display ---
        JPanel clockPanel = new JPanel();
        clockPanel.setLayout(new BoxLayout(clockPanel, BoxLayout.Y_AXIS));
        clockPanel.setBackground(Color.WHITE);
        clockPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel clockTitle = new JLabel("Simulated Time");
        clockTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        clockTitle.setForeground(new Color(100, 116, 139));
        clockTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        timeLabel = new JLabel(timer.getFormattedTime());
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        timeLabel.setForeground(new Color(15, 23, 42));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel stateTitle = new JLabel("System State");
        stateTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        stateTitle.setForeground(new Color(100, 116, 139));
        stateTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        stateBadge = new JLabel(system.getState().toString());
        stateBadge.setFont(new Font("SansSerif", Font.BOLD, 13));
        stateBadge.setForeground(Color.WHITE);
        stateBadge.setOpaque(true);
        stateBadge.setBackground(DORMANT_COLOR);
        stateBadge.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        stateBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        clockPanel.add(clockTitle);
        clockPanel.add(Box.createVerticalStrut(4));
        clockPanel.add(timeLabel);
        clockPanel.add(Box.createVerticalStrut(12));
        clockPanel.add(stateTitle);
        clockPanel.add(Box.createVerticalStrut(4));
        clockPanel.add(stateBadge);

        // --- MIDDLE: Skip controls ---
        JPanel skipPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        skipPanel.setBackground(new Color(248, 250, 252));
        skipPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            "Time Controls"
        ));

        JButton skip5Min = makeButton("⏩  +5 Minutes");
        JButton skip1Day = makeButton("📅  +1 Day");
        JButton skipCustom = makeButton("⌨  Custom Skip...");
        JButton skipToMilestone = makeButton("🎯  Jump to Milestone...");

        skip5Min.addActionListener(e -> {
            timer.skipMinutes(5);
        });

        skip1Day.addActionListener(e -> {
            timer.skipDay();
        });

        skipCustom.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this,
                "Enter number of minutes to skip:", "Custom Skip", JOptionPane.PLAIN_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int mins = Integer.parseInt(input.trim());
                    if (mins > 0) timer.skipMinutes(mins);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number.");
                }
            }
        });

        skipToMilestone.addActionListener(e -> {
            String[] options = {
                "Bookings Open",
                "Bookings Closed",
                "Fair Start",
                "Fair End"
            };
            int choice = JOptionPane.showOptionDialog(this,
                "Jump to which milestone?", "Jump to Milestone",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

            switch (choice) {
                case 0: timer.jumpToBookingsOpen(); break;
                case 1: timer.jumpToBookingsClosed(); break;
                case 2: timer.jumpToFairStart(); break;
                case 3: timer.jumpToFairEnd(); break;
            }
        });

        skipPanel.add(skip5Min);
        skipPanel.add(skip1Day);
        skipPanel.add(skipCustom);
        skipPanel.add(skipToMilestone);

        // --- Assemble ---
        root.add(clockPanel, BorderLayout.NORTH);
        root.add(skipPanel, BorderLayout.CENTER);

        // Footer note
        JLabel footer = new JLabel("Time is one-directional — you cannot go backwards.");
        footer.setFont(new Font("SansSerif", Font.ITALIC, 10));
        footer.setForeground(new Color(148, 163, 184));
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Called by SystemTimer whenever time advances.
     * Updates the clock display and state badge on the screen.
     */
    @Override
    public void onTimeChanged(LocalDateTime newTime) {
        SwingUtilities.invokeLater(() -> {
            timeLabel.setText(newTime.format(FORMATTER));
            String stateName = system.getState().toString();
            stateBadge.setText(stateName);
            stateBadge.setBackground(getBadgeColor(stateName));
        });
    }

    private Color getBadgeColor(String state) {
        switch (state) {
            case "DORMANT":         return DORMANT_COLOR;
            case "PREPARING":       return PREPARING_COLOR;
            case "BOOKINGS_OPEN":   return BOOKINGS_OPEN_COLOR;
            case "BOOKINGS_CLOSED": return BOOKINGS_CLOSED_COLOR;
            case "FAIR_LIVE":       return FAIR_LIVE_COLOR;
            default:                return DORMANT_COLOR;
        }
    }
}
