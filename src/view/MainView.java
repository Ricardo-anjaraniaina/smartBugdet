package view;

import controller.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainView extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private DashboardView dashboardView;
    private TransactionsView transactionsView;
    private BudgetView budgetView;
    private SavingsView savingsView;
    private StatisticsView statisticsView;

    public MainView() {
        setTitle("SmartBudget — " + SessionManager.getUsername());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        contentPanel.setBackground(AppColors.BACKGROUND);
        dashboardView    = new DashboardView();
        transactionsView = new TransactionsView();
        budgetView       = new BudgetView();
        savingsView      = new SavingsView();
        statisticsView   = new StatisticsView();

        contentPanel.add(dashboardView,    "dashboard");
        contentPanel.add(transactionsView, "transactions");
        contentPanel.add(budgetView,       "budgets");
        contentPanel.add(savingsView,      "savings");
        contentPanel.add(statisticsView,   "statistics");

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "dashboard");

        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(AppColors.SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // App name
        JLabel appName = new JLabel("SmartBudget");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appName.setForeground(Color.WHITE);
        appName.setBorder(BorderFactory.createEmptyBorder(28, 20, 28, 20));
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(appName);

        // Separator
        sidebar.add(separator());

        String[][] items = {
            {">", "Dashboard",     "dashboard"},
            {">", "Transactions",  "transactions"},
            {">", "Budgets",       "budgets"},
            {">", "Epargne",       "savings"},
            {">", "Statistiques",  "statistics"},
        };

        for (String[] item : items) {
            sidebar.add(navButton(item[0], item[1], item[2]));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(separator());

        // User info
        JLabel userLabel = new JLabel("Utilisateur : " + SessionManager.getUsername());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(AppColors.SIDEBAR_TEXT);
        userLabel.setBorder(BorderFactory.createEmptyBorder(12, 20, 4, 20));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(userLabel);

        sidebar.add(navButtonAction(">", "Deconnexion", () -> {
            SessionManager.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
        }));

        sidebar.add(Box.createVerticalStrut(16));
        return sidebar;
    }

    private JPanel navButton(String icon, String label, String card) {
        return navButtonAction(icon, label, () -> {
            cardLayout.show(contentPanel, card);
            refreshView(card);
        });
    }

    private void refreshView(String card) {
        switch (card) {
            case "dashboard"    -> dashboardView.refresh();
            case "transactions" -> transactionsView.refresh();
            case "budgets"      -> budgetView.refresh();
            case "savings"      -> savingsView.refresh();
            case "statistics"   -> statisticsView.refresh();
        }
    }

    private JPanel navButtonAction(String icon, String label, Runnable action) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        panel.setBackground(AppColors.SIDEBAR_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(icon + "  " + label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(AppColors.SIDEBAR_TEXT);
        panel.add(lbl);

        panel.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { panel.setBackground(AppColors.SIDEBAR_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { panel.setBackground(AppColors.SIDEBAR_BG); }
            @Override public void mouseClicked(MouseEvent e) { action.run(); }
        });
        return panel;
    }

    private JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x334155));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }
}
