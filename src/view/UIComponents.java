package view;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class UIComponents {

    // ── Rounded border ──────────────────────────────────────────────────────
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        public RoundedBorder(int radius, Color color) { this.radius = radius; this.color = color; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
    }

    // ── Rounded panel ───────────────────────────────────────────────────────
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private Color bg;
        public RoundedPanel(int radius, Color bg) {
            this.radius = radius; this.bg = bg;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Modern button ───────────────────────────────────────────────────────
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? AppColors.PRIMARY_DARK :
                            getModel().isRollover() ? AppColors.PRIMARY_DARK : AppColors.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(160, 40));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = primaryButton(text);
        btn.addPropertyChangeListener("model", e -> {});
        // Override paint to use DANGER color
        btn.setBackground(AppColors.DANGER);
        return new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(0xB91C1C);
                g2.setColor(getModel().isRollover() ? base : AppColors.DANGER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
            { // init block
                setPreferredSize(new Dimension(120, 36));
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        };
    }

    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? AppColors.BORDER : AppColors.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(AppColors.PRIMARY);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setColor(AppColors.PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(120, 36));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Amount field (chiffres + un seul point décimal) ───────────────────
    public static JTextField amountField(int columns) {
        JTextField f = styledField(columns);
        ((javax.swing.text.AbstractDocument) f.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                private boolean isValid(String s) {
                    return s.matches("\\d*\\.?\\d*");
                }
                @Override
                public void insertString(javax.swing.text.DocumentFilter.FilterBypass fb,
                        int off, String s, javax.swing.text.AttributeSet a)
                        throws javax.swing.text.BadLocationException {
                    String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String next = cur.substring(0, off) + s + cur.substring(off);
                    if (isValid(next)) super.insertString(fb, off, s, a);
                }
                @Override
                public void replace(javax.swing.text.DocumentFilter.FilterBypass fb,
                        int off, int len, String s, javax.swing.text.AttributeSet a)
                        throws javax.swing.text.BadLocationException {
                    String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String next = cur.substring(0, off) + (s != null ? s : "") + cur.substring(off + len);
                    if (isValid(next)) super.replace(fb, off, len, s, a);
                }
            });
        return f;
    }

    // ── Styled text field ───────────────────────────────────────────────────
    public static JTextField styledField(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, AppColors.BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    public static JPasswordField styledPasswordField(int columns) {
        JPasswordField f = new JPasswordField(columns);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, AppColors.BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    // ── Card panel ──────────────────────────────────────────────────────────
    public static JPanel card(String title, String value, Color accent) {
        RoundedPanel card = new RoundedPanel(12, AppColors.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Left accent bar
        JPanel accent_bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        accent_bar.setPreferredSize(new Dimension(4, 0));
        accent_bar.setOpaque(false);
        card.add(accent_bar, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(AppColors.TEXT_LIGHT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(AppColors.TEXT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(valueLabel);
        card.add(content, BorderLayout.CENTER);

        // Drop shadow effect via border
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(4, 4, 8, 8),
            BorderFactory.createCompoundBorder(
                new RoundedBorder(12, new Color(0, 0, 0, 20)),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
            )
        ));
        card.add(accent_bar, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // ── Section title ───────────────────────────────────────────────────────
    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(AppColors.TEXT);
        return l;
    }

    // ── Progress bar ────────────────────────────────────────────────────────
    public static JProgressBar styledProgressBar(double percentage) {
        int val = (int) Math.min(percentage, 100);
        JProgressBar pb = new JProgressBar(0, 100);
        pb.setValue(val);
        pb.setStringPainted(false);
        pb.setPreferredSize(new Dimension(200, 10));
        pb.setBorderPainted(false);
        Color color = percentage >= 100 ? AppColors.DANGER :
                      percentage >= 90  ? AppColors.DANGER :
                      percentage >= 70  ? AppColors.WARNING : AppColors.SUCCESS;
        pb.setForeground(color);
        pb.setBackground(AppColors.BORDER);
        return pb;
    }

    // ── Styled table ─────────────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(AppColors.WHITE);
        table.setSelectionBackground(new Color(0xEFF6FF));
        table.setSelectionForeground(AppColors.TEXT);
        table.setForeground(AppColors.TEXT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(AppColors.BACKGROUND);
        table.getTableHeader().setForeground(AppColors.TEXT_LIGHT);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER));
    }
}
