package view;

import controller.SessionManager;
import controller.TransactionController;
import database.TransactionDAO;
import model.Transaction;
import utils.FormatUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsView extends JPanel {

    private final TransactionController ctrl = new TransactionController();
    private final TransactionDAO dao = new TransactionDAO();

    private JPanel statsPanel, chartsPanel, comparisonPanel;
    private JSpinner month1Spinner, year1Spinner, month2Spinner, year2Spinner;

    public StatisticsView() {
        setLayout(new BorderLayout());
        setBackground(AppColors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        JLabel title = new JLabel("Statistiques & Graphiques");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(AppColors.TEXT);
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.setBackground(AppColors.BACKGROUND);

        statsPanel      = new JPanel();
        statsPanel.setBackground(AppColors.BACKGROUND);
        chartsPanel     = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsPanel.setBackground(AppColors.BACKGROUND);
        comparisonPanel = buildComparisonPanel();

        tabs.addTab("Résumé", new JScrollPane(statsPanel));
        tabs.addTab("Graphiques", new JScrollPane(chartsPanel));
        tabs.addTab("Comparaison mensuelle", comparisonPanel);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        center.add(tabs, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    public void refresh() {
        refreshStats();
        refreshCharts();
    }

    private void refreshStats() {
        statsPanel.removeAll();
        statsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        try {
            int userId = SessionManager.getUserId();
            List<Transaction> all = ctrl.getAll(userId);

            BigDecimal totalIncome  = sum(all, "INCOME");
            BigDecimal totalExpense = sum(all, "EXPENSE");
            BigDecimal balance      = totalIncome.subtract(totalExpense);
            BigDecimal avgExpense   = all.stream().filter(t -> "EXPENSE".equals(t.getType()))
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            long expCount = all.stream().filter(t -> "EXPENSE".equals(t.getType())).count();
            BigDecimal avg = expCount > 0 ? avgExpense.divide(BigDecimal.valueOf(expCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            BigDecimal maxExp = all.stream().filter(t -> "EXPENSE".equals(t.getType()))
                .map(Transaction::getAmount).max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);

            double savingsRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? balance.divide(totalIncome, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0;

            // Top category
            Map<String, BigDecimal> byCat = all.stream().filter(t -> "EXPENSE".equals(t.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategoryName,
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
            String topCat = byCat.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("—");

            String[][] rows = {
                {"Total revenus",              FormatUtils.formatAmount(totalIncome)},
                {"Total dépenses",             FormatUtils.formatAmount(totalExpense)},
                {"Solde",                      FormatUtils.formatAmount(balance)},
                {"Dépense moyenne",            FormatUtils.formatAmount(avg)},
                {"Plus grosse dépense",        FormatUtils.formatAmount(maxExp)},
                {"Nombre de transactions",     String.valueOf(all.size())},
                {"Catégorie la plus dépensée", topCat},
                {"Taux d'épargne",             FormatUtils.formatPercentage(savingsRate)},
            };

            for (int i = 0; i < rows.length; i++) {
                gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.5;
                JLabel lbl = new JLabel(rows[i][0]);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lbl.setForeground(AppColors.TEXT_LIGHT);
                statsPanel.add(lbl, gbc);

                gbc.gridx = 1;
                JLabel val = new JLabel(rows[i][1]);
                val.setFont(new Font("Segoe UI", Font.BOLD, 14));
                val.setForeground(AppColors.TEXT);
                statsPanel.add(val, gbc);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private void refreshCharts() {
        chartsPanel.removeAll();
        try {
            int userId = SessionManager.getUserId();
            List<Transaction> all = ctrl.getAll(userId);

            // Pie chart: expenses by category
            Map<String, BigDecimal> byCat = all.stream().filter(t -> "EXPENSE".equals(t.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategoryName,
                    Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
            chartsPanel.add(new PieChartPanel("Dépenses par catégorie", byCat));

            // Bar chart: monthly income vs expense (last 6 months)
            chartsPanel.add(new BarChartPanel("Revenus / Dépenses mensuels", userId, dao));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }

    // ── Pie Chart ────────────────────────────────────────────────────────────
    static class PieChartPanel extends JPanel {
        private final String title;
        private final Map<String, BigDecimal> data;
        private static final Color[] COLORS = {
            new Color(0x2563EB), new Color(0x16A34A), new Color(0xDC2626),
            new Color(0xF59E0B), new Color(0x7C3AED), new Color(0x0891B2),
            new Color(0xDB2777), new Color(0x65A30D)
        };

        PieChartPanel(String title, Map<String, BigDecimal> data) {
            this.title = title;
            this.data = data;
            setBackground(AppColors.WHITE);
            setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Title
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(AppColors.TEXT);
            g2.drawString(title, 16, 24);

            if (data.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(AppColors.TEXT_LIGHT);
                g2.drawString("Aucune donnée", getWidth()/2 - 40, getHeight()/2);
                g2.dispose();
                return;
            }

            BigDecimal total = data.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            int size = Math.min(getWidth(), getHeight()) - 120;
            int x = (getWidth() - size) / 2 - 40;
            int y = 40;

            double startAngle = 0;
            int i = 0;
            List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(data.entrySet());
            for (Map.Entry<String, BigDecimal> entry : entries) {
                double pct = entry.getValue().divide(total, 4, RoundingMode.HALF_UP).doubleValue();
                double arc = pct * 360;
                g2.setColor(COLORS[i % COLORS.length]);
                g2.fill(new Arc2D.Double(x, y, size, size, startAngle, arc, Arc2D.PIE));
                startAngle += arc;
                i++;
            }

            // Legend
            int legendX = getWidth() - 160;
            int legendY = 50;
            i = 0;
            for (Map.Entry<String, BigDecimal> entry : entries) {
                double pct = entry.getValue().divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100;
                g2.setColor(COLORS[i % COLORS.length]);
                g2.fillRect(legendX, legendY + i * 22, 14, 14);
                g2.setColor(AppColors.TEXT);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(entry.getKey() + " " + String.format("%.0f%%", pct), legendX + 18, legendY + i * 22 + 12);
                i++;
            }
            g2.dispose();
        }
    }

    // ── Bar Chart ────────────────────────────────────────────────────────────
    static class BarChartPanel extends JPanel {
        private final String title;
        private final int userId;
        private final TransactionDAO dao;

        BarChartPanel(String title, int userId, TransactionDAO dao) {
            this.title = title;
            this.userId = userId;
            this.dao = dao;
            setBackground(AppColors.WHITE);
            setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(AppColors.TEXT);
            g2.drawString(title, 16, 24);

            LocalDate now = LocalDate.now();
            int months = 6;
            BigDecimal[] incomes  = new BigDecimal[months];
            BigDecimal[] expenses = new BigDecimal[months];
            String[] labels = new String[months];
            BigDecimal maxVal = BigDecimal.ONE;

            for (int i = 0; i < months; i++) {
                LocalDate d = now.minusMonths(months - 1 - i);
                try {
                    incomes[i]  = dao.sumByTypeAndMonth(userId, "INCOME",  d.getMonthValue(), d.getYear());
                    expenses[i] = dao.sumByTypeAndMonth(userId, "EXPENSE", d.getMonthValue(), d.getYear());
                } catch (SQLException e) {
                    incomes[i] = expenses[i] = BigDecimal.ZERO;
                }
                labels[i] = FormatUtils.getMonthName(d.getMonthValue()).substring(0, 3);
                if (incomes[i].compareTo(maxVal) > 0)  maxVal = incomes[i];
                if (expenses[i].compareTo(maxVal) > 0) maxVal = expenses[i];
            }

            int chartX = 50, chartY = 40;
            int chartW = getWidth() - 80, chartH = getHeight() - 100;
            int barGroupW = chartW / months;
            int barW = barGroupW / 3;

            // Axes
            g2.setColor(AppColors.BORDER);
            g2.drawLine(chartX, chartY, chartX, chartY + chartH);
            g2.drawLine(chartX, chartY + chartH, chartX + chartW, chartY + chartH);

            for (int i = 0; i < months; i++) {
                int groupX = chartX + i * barGroupW + barGroupW / 6;

                // Income bar
                int incH = maxVal.compareTo(BigDecimal.ZERO) > 0
                    ? (int)(incomes[i].divide(maxVal, 4, RoundingMode.HALF_UP).doubleValue() * chartH) : 0;
                g2.setColor(AppColors.SUCCESS);
                g2.fillRoundRect(groupX, chartY + chartH - incH, barW, incH, 4, 4);

                // Expense bar
                int expH = maxVal.compareTo(BigDecimal.ZERO) > 0
                    ? (int)(expenses[i].divide(maxVal, 4, RoundingMode.HALF_UP).doubleValue() * chartH) : 0;
                g2.setColor(AppColors.DANGER);
                g2.fillRoundRect(groupX + barW + 2, chartY + chartH - expH, barW, expH, 4, 4);

                // Label
                g2.setColor(AppColors.TEXT_LIGHT);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(labels[i], groupX, chartY + chartH + 16);
            }

            // Legend
            g2.setColor(AppColors.SUCCESS);
            g2.fillRect(chartX, chartY + chartH + 28, 12, 12);
            g2.setColor(AppColors.TEXT);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString("Revenus", chartX + 16, chartY + chartH + 40);
            g2.setColor(AppColors.DANGER);
            g2.fillRect(chartX + 80, chartY + chartH + 28, 12, 12);
            g2.setColor(AppColors.TEXT);
            g2.drawString("Dépenses", chartX + 96, chartY + chartH + 40);

            g2.dispose();
        }
    }

    // ── Comparison panel ─────────────────────────────────────────────────────
    private JPanel buildComparisonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppColors.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        LocalDate now = LocalDate.now();
        month1Spinner = new JSpinner(new SpinnerNumberModel(now.minusMonths(1).getMonthValue(), 1, 12, 1));
        year1Spinner  = new JSpinner(new SpinnerNumberModel(now.minusMonths(1).getYear(), 2020, 2100, 1));
        month2Spinner = new JSpinner(new SpinnerNumberModel(now.getMonthValue(), 1, 12, 1));
        year2Spinner  = new JSpinner(new SpinnerNumberModel(now.getYear(), 2020, 2100, 1));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        controls.setOpaque(false);
        controls.add(new JLabel("Mois 1 :"));
        controls.add(month1Spinner);
        controls.add(year1Spinner);
        controls.add(new JLabel("  vs  Mois 2 :"));
        controls.add(month2Spinner);
        controls.add(year2Spinner);

        JButton compareBtn = UIComponents.primaryButton("Comparer");
        compareBtn.setPreferredSize(new Dimension(110, 34));
        controls.add(compareBtn);

        JPanel resultPanel = new JPanel();
        resultPanel.setBackground(AppColors.BACKGROUND);
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));

        compareBtn.addActionListener(e -> {
            resultPanel.removeAll();
            try {
                int userId = SessionManager.getUserId();
                int m1 = (int) month1Spinner.getValue(), y1 = (int) year1Spinner.getValue();
                int m2 = (int) month2Spinner.getValue(), y2 = (int) year2Spinner.getValue();

                BigDecimal inc1 = dao.sumByTypeAndMonth(userId, "INCOME",  m1, y1);
                BigDecimal exp1 = dao.sumByTypeAndMonth(userId, "EXPENSE", m1, y1);
                BigDecimal inc2 = dao.sumByTypeAndMonth(userId, "INCOME",  m2, y2);
                BigDecimal exp2 = dao.sumByTypeAndMonth(userId, "EXPENSE", m2, y2);

                BigDecimal sav1 = inc1.subtract(exp1);
                BigDecimal sav2 = inc2.subtract(exp2);

                String label1 = FormatUtils.getMonthName(m1) + " " + y1;
                String label2 = FormatUtils.getMonthName(m2) + " " + y2;

                UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, AppColors.WHITE);
                card.setLayout(new GridBagLayout());
                card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
                card.setMaximumSize(new Dimension(600, 300));

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(8, 16, 8, 16);
                gbc.fill = GridBagConstraints.HORIZONTAL;

                addCompRow(card, gbc, 0, "", label1, label2, "Évolution");
                addCompRow(card, gbc, 1, "Revenus",  FormatUtils.formatAmount(inc1), FormatUtils.formatAmount(inc2), diff(inc1, inc2));
                addCompRow(card, gbc, 2, "Dépenses", FormatUtils.formatAmount(exp1), FormatUtils.formatAmount(exp2), diff(exp1, exp2));
                addCompRow(card, gbc, 3, "Épargne",  FormatUtils.formatAmount(sav1), FormatUtils.formatAmount(sav2), diff(sav1, sav2));

                resultPanel.add(card);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
            resultPanel.revalidate();
            resultPanel.repaint();
        });

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultPanel), BorderLayout.CENTER);
        return panel;
    }

    private void addCompRow(JPanel p, GridBagConstraints gbc, int row,
                             String label, String v1, String v2, String diff) {
        Font bold  = new Font("Segoe UI", Font.BOLD, 13);
        Font plain = new Font("Segoe UI", Font.PLAIN, 13);
        gbc.gridy = row;
        gbc.gridx = 0; p.add(styledLabel(label, bold, AppColors.TEXT_LIGHT), gbc);
        gbc.gridx = 1; p.add(styledLabel(v1, plain, AppColors.TEXT), gbc);
        gbc.gridx = 2; p.add(styledLabel(v2, plain, AppColors.TEXT), gbc);
        gbc.gridx = 3;
        Color c = diff.startsWith("+") ? AppColors.SUCCESS : diff.startsWith("-") ? AppColors.DANGER : AppColors.TEXT_LIGHT;
        p.add(styledLabel(diff, bold, c), gbc);
    }

    private JLabel styledLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private String diff(BigDecimal v1, BigDecimal v2) {
        if (v1.compareTo(BigDecimal.ZERO) == 0) return "—";
        BigDecimal pct = v2.subtract(v1).divide(v1, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        double d = pct.doubleValue();
        return (d >= 0 ? "+" : "") + String.format("%.1f%%", d);
    }

    private BigDecimal sum(List<Transaction> list, String type) {
        return list.stream().filter(t -> type.equals(t.getType()))
            .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
