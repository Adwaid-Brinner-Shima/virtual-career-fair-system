package vcfs.ui;
 
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
 
/**
 * Reusable static Swing factory methods and colour constants
 * shared by AdministratorScreen, CandidateScreen, and panel builders.
 */
public final class CandidateUI {
 
    private CandidateUI() {}
 
    // ── Colour palette (identical to AdministratorScreen) ─────────────────
    public static final Color PRIMARY = new Color(30,  41,  59);
    public static final Color ACCENT  = new Color(59, 130, 246);
    public static final Color BG      = new Color(248, 250, 252);
    public static final Color WHITE   = Color.WHITE;
    public static final Color BORDER  = new Color(226, 232, 240);
    public static final Color MUTED   = new Color(100, 116, 139);
    public static final Color DANGER  = new Color(239,  68,  68);
 
    // ── Label factories ───────────────────────────────────────────────────
    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        l.setForeground(PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
 
    public static JLabel infoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.ITALIC, 11));
        l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
 
    // ── Input factories ───────────────────────────────────────────────────
    public static JTextField styledField(String value) {
        JTextField f = new JTextField(value, 20);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }
 
    public static JPanel labelledField(String labelText, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(500, 40));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(PRIMARY);
        lbl.setPreferredSize(new Dimension(160, 30));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }
 
    // ── Button factories ──────────────────────────────────────────────────
    public static JButton accentButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setBackground(ACCENT); b.setForeground(WHITE);
        b.setFocusPainted(false); b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
 
    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setBackground(DANGER); b.setForeground(WHITE);
        b.setFocusPainted(false); b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
 
    // ── Table / scroll factories ──────────────────────────────────────────
    public static JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setRowHeight(28); t.setGridColor(BORDER);
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(PRIMARY);
        t.setShowVerticalLines(false);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(241, 245, 249));
        t.getTableHeader().setForeground(PRIMARY);
        return t;
    }
 
    public static JScrollPane scrolled(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        return sp;
    }
 
    /** Creates a left-aligned FlowLayout panel containing the given buttons. */
    public static JPanel buttonRow(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBackground(BG);
        for (JButton b : buttons) p.add(b);
        return p;
    }
 
    // ── Dialog helpers ────────────────────────────────────────────────────
    public static void ok(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
 
    public static void err(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
 