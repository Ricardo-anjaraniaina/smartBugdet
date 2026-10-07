package view;

import javax.swing.*;
import java.awt.*;
import java.awt.Dialog;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Champ de saisie de date avec bouton calendrier popup.
 * Utilisation : new DatePickerField() — getDate() / setDate(LocalDate)
 */
public class DatePickerField extends JPanel {

    private final JTextField textField;
    private final JButton calBtn;

    public DatePickerField() {
        setLayout(new BorderLayout(2, 0));
        setOpaque(false);

        textField = UIComponents.styledField(10);
        textField.setToolTipText("yyyy-MM-dd");

        calBtn = new JButton("...");
        calBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        calBtn.setPreferredSize(new Dimension(30, textField.getPreferredSize().height));
        calBtn.setFocusPainted(false);
        calBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calBtn.setBackground(AppColors.PRIMARY);
        calBtn.setForeground(Color.WHITE);
        calBtn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        calBtn.addActionListener(e -> showCalendarPopup());

        add(textField, BorderLayout.CENTER);
        add(calBtn, BorderLayout.EAST);
    }

    public String getText() { return textField.getText(); }
    public void setText(String s) { textField.setText(s); }

    public LocalDate getDate() {
        String s = textField.getText().trim();
        if (s.isEmpty()) return null;
        try { return LocalDate.parse(s); }
        catch (DateTimeParseException e) { return null; }
    }

    public void setDate(LocalDate date) {
        textField.setText(date != null ? date.toString() : "");
    }

    private void showCalendarPopup() {
        LocalDate initial = getDate();
        if (initial == null) initial = LocalDate.now();

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog popup = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        popup.setUndecorated(true);
        popup.setSize(260, 220);

        CalendarPanel cal = new CalendarPanel(initial, chosen -> {
            setDate(chosen);
            popup.dispose();
        });
        popup.add(cal);

        Point loc = calBtn.getLocationOnScreen();
        popup.setLocation(loc.x - 230, loc.y + calBtn.getHeight());

        // Fermer si clic en dehors
        popup.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent e) {}
            public void windowLostFocus(java.awt.event.WindowEvent e) { popup.dispose(); }
        });

        popup.setVisible(true);
    }

    // ── Inner calendar panel ──────────────────────────────────────────────────

    interface DateSelectedListener { void onDateSelected(LocalDate date); }

    private static class CalendarPanel extends JPanel {

        private YearMonth current;
        private final DateSelectedListener listener;
        private JLabel monthLabel;
        private JPanel daysPanel;

        CalendarPanel(LocalDate initial, DateSelectedListener listener) {
            this.current  = YearMonth.from(initial);
            this.listener = listener;
            setLayout(new BorderLayout(0, 4));
            setBackground(AppColors.WHITE);
            setBorder(BorderFactory.createLineBorder(AppColors.BORDER, 1));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

            add(buildNav(), BorderLayout.NORTH);
            daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
            daysPanel.setBackground(AppColors.WHITE);
            add(daysPanel, BorderLayout.CENTER);
            renderDays();
        }

        private JPanel buildNav() {
            JPanel nav = new JPanel(new BorderLayout());
            nav.setBackground(AppColors.WHITE);

            JButton prev = navBtn("<");
            JButton next = navBtn(">");
            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            monthLabel.setForeground(AppColors.TEXT);

            prev.addActionListener(e -> { current = current.minusMonths(1); renderDays(); });
            next.addActionListener(e -> { current = current.plusMonths(1);  renderDays(); });

            nav.add(prev, BorderLayout.WEST);
            nav.add(monthLabel, BorderLayout.CENTER);
            nav.add(next, BorderLayout.EAST);
            return nav;
        }

        private JButton navBtn(String text) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setBackground(AppColors.WHITE);
            b.setForeground(AppColors.PRIMARY);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }

        private void renderDays() {
            String[] months = {"Janv","Fevr","Mars","Avr","Mai","Juin",
                               "Juil","Aout","Sept","Oct","Nov","Dec"};
            monthLabel.setText(months[current.getMonthValue() - 1] + " " + current.getYear());

            daysPanel.removeAll();

            // En-têtes jours
            String[] days = {"Lu","Ma","Me","Je","Ve","Sa","Di"};
            for (String d : days) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lbl.setForeground(AppColors.TEXT_LIGHT);
                daysPanel.add(lbl);
            }

            // Décalage : lundi = 1
            int firstDow = current.atDay(1).getDayOfWeek().getValue(); // 1=lundi
            for (int i = 1; i < firstDow; i++) daysPanel.add(new JLabel(""));

            LocalDate today = LocalDate.now();
            for (int day = 1; day <= current.lengthOfMonth(); day++) {
                LocalDate date = current.atDay(day);
                JButton btn = new JButton(String.valueOf(day));
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.setMargin(new Insets(1, 1, 1, 1));

                if (date.equals(today)) {
                    btn.setBackground(AppColors.PRIMARY);
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setBackground(AppColors.WHITE);
                    btn.setForeground(AppColors.TEXT);
                }

                btn.addActionListener(e -> listener.onDateSelected(date));
                daysPanel.add(btn);
            }

            daysPanel.revalidate();
            daysPanel.repaint();
        }
    }
}
