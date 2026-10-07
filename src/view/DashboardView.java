package view;

import controller.SessionManager;
import controller.TransactionController;
import controller.BudgetController;
import controller.SavingsController;
import database.UserDAO;
import model.Budget;
import model.SavingsGoal;
import model.Transaction;
import utils.FormatUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardView extends JPanel {

    private final TransactionController txCtrl = new TransactionController();
    private final BudgetController budgetCtrl  = new BudgetController();
    private final SavingsController savingsCtrl = new SavingsController();
    private final UserDAO userDAO = new UserDAO();

    private JLabel balanceValue, incomeValue, expenseValue, savingsValue;
    private DefaultTableModel tableModel;
    private JPanel alertsPanel;

    public DashboardView() {
        setLayout(new BorderLayout());
        setBackground(AppColors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        // Header
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(AppColors.TEXT);

        JButton editBalanceBtn = UIComponents.secondaryButton("Solde initial");
        editBalanceBtn.setPreferredSize(new Dimension(140, 34));
        editBalanceBtn.addActionListener(e -> showInitialBalanceDialog());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(editBalanceBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Summary cards
        center.add(buildSummaryCards());
        center.add(Box.createVerticalStrut(24));

        // Bottom: recent transactions + alerts
        JPanel bottom = new JPanel(new GridLayout(1, 2, 20, 0));
        bottom.setOpaque(false);
        bottom.add(buildRecentTransactions());
        bottom.add(buildAlerts());
        center.add(bottom);

        add(center, BorderLayout.CENTER);
    }

    private JPanel buildSummaryCards() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 16, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        balanceValue  = new JLabel("0 Ar");
        incomeValue   = new JLabel("0 Ar");
        expenseValue  = new JLabel("0 Ar");
        savingsValue  = new JLabel("0 Ar");

        panel.add(summaryCard("Solde actuel",   balanceValue,  AppColors.PRIMARY));
        panel.add(summaryCard("Revenus",          incomeValue,   AppColors.SUCCESS));
        panel.add(summaryCard("Depenses",         expenseValue,  AppColors.DANGER));
        panel.add(summaryCard("Epargne totale",   savingsValue,  AppColors.WARNING));
        return panel;
    }

    private JPanel summaryCard(String title, JLabel valueLabel, Color accent) {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, AppColors.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(AppColors.TEXT_LIGHT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        valueLabel.setForeground(accent);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(8));
        content.add(valueLabel);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRecentTransactions() {
        UIComponents.RoundedPanel panel = new UIComponents.RoundedPanel(12, AppColors.WHITE);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = UIComponents.sectionTitle("Transactions récentes");
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Date", "Description", "Catégorie", "Type", "Montant"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        UIComponents.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppColors.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAlerts() {
        UIComponents.RoundedPanel panel = new UIComponents.RoundedPanel(12, AppColors.WHITE);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = UIComponents.sectionTitle("Alertes");
        panel.add(title, BorderLayout.NORTH);

        alertsPanel = new JPanel();
        alertsPanel.setOpaque(false);
        alertsPanel.setLayout(new BoxLayout(alertsPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(alertsPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppColors.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    public void refresh() {
        int userId = SessionManager.getUserId();
        try {
            // Summary
            BigDecimal initialBalance = userDAO.getInitialBalance(userId);
            BigDecimal income  = txCtrl.getAll(userId).stream()
                .filter(t -> "INCOME".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal expense = txCtrl.getAll(userId).stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal balance = initialBalance.add(income).subtract(expense);

            BigDecimal savings = savingsCtrl.getGoals(userId).stream()
                .map(SavingsGoal::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            balanceValue.setText(FormatUtils.formatAmount(balance));
            incomeValue.setText(FormatUtils.formatAmount(income));
            expenseValue.setText(FormatUtils.formatAmount(expense));
            savingsValue.setText(FormatUtils.formatAmount(savings));

            // Recent transactions
            tableModel.setRowCount(0);
            for (Transaction t : txCtrl.getRecent(userId, 8)) {
                tableModel.addRow(new Object[]{
                    FormatUtils.formatDate(t.getTransactionDate()),
                    t.getDescription(),
                    t.getCategoryName(),
                    "INCOME".equals(t.getType()) ? "Revenu" : "Dépense",
                    FormatUtils.formatAmount(t.getAmount())
                });
            }

            // Alerts
            alertsPanel.removeAll();
            LocalDate now = LocalDate.now();
            List<Budget> budgets = budgetCtrl.getBudgets(userId, now.getMonthValue(), now.getYear());
            for (Budget b : budgets) {
                double pct = b.getPercentage();
                if (pct >= 100) {
                    addAlert("[!] Budget " + b.getCategoryName() + " depasse (" + FormatUtils.formatPercentage(pct) + ")", AppColors.DANGER);
                } else if (pct >= 90) {
                    addAlert("[!] Budget " + b.getCategoryName() + " presque atteint (" + FormatUtils.formatPercentage(pct) + ")", AppColors.WARNING);
                } else if (pct >= 70) {
                    addAlert("[i] Budget " + b.getCategoryName() + " a " + FormatUtils.formatPercentage(pct), AppColors.WARNING);
                }
            }

            for (SavingsGoal g : savingsCtrl.getGoals(userId)) {
                double pct = g.getProgressPercentage();
                if (pct >= 100) {
                    addAlert("[OK] Objectif \"" + g.getName() + "\" atteint !", AppColors.SUCCESS);
                } else if (pct >= 50) {
                    addAlert("[>] Objectif \"" + g.getName() + "\" a " + FormatUtils.formatPercentage(pct), AppColors.PRIMARY);
                }
            }

            if (alertsPanel.getComponentCount() == 0) {
                JLabel ok = new JLabel("[OK] Tout va bien !");
                ok.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                ok.setForeground(AppColors.SUCCESS);
                ok.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
                alertsPanel.add(ok);
            }

            alertsPanel.revalidate();
            alertsPanel.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showInitialBalanceDialog() {
        try {
            BigDecimal current = userDAO.getInitialBalance(SessionManager.getUserId());
            JTextField field = UIComponents.amountField(15);
            field.setText(current.toPlainString());
            int result = JOptionPane.showConfirmDialog(this,
                new Object[]{"Solde initial du compte (Ar) :", field},
                "Solde initial", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION && !field.getText().isBlank()) {
                userDAO.updateInitialBalance(SessionManager.getUserId(), new BigDecimal(field.getText().trim()));
                refresh();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addAlert(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(color);
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        alertsPanel.add(lbl);
    }
}
