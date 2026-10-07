package view;

import controller.BudgetController;
import controller.SessionManager;
import controller.TransactionController;
import model.Budget;
import model.Category;
import utils.FormatUtils;
import utils.ValidationUtils;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BudgetView extends JPanel {

    private final BudgetController ctrl = new BudgetController();
    private final TransactionController txCtrl = new TransactionController();

    private JPanel budgetsPanel;
    private JSpinner monthSpinner, yearSpinner;
    private List<Budget> currentBudgets;

    public BudgetView() {
        setLayout(new BorderLayout());
        setBackground(AppColors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        JLabel title = new JLabel("Budgets");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(AppColors.TEXT);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        center.add(buildControls(), BorderLayout.NORTH);

        budgetsPanel = new JPanel();
        budgetsPanel.setOpaque(false);
        budgetsPanel.setLayout(new BoxLayout(budgetsPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(budgetsPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppColors.BACKGROUND);
        center.add(scroll, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private JPanel buildControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        panel.setOpaque(false);

        LocalDate now = LocalDate.now();
        monthSpinner = new JSpinner(new SpinnerNumberModel(now.getMonthValue(), 1, 12, 1));
        yearSpinner  = new JSpinner(new SpinnerNumberModel(now.getYear(), 2020, 2100, 1));

        JButton loadBtn = UIComponents.primaryButton("Afficher");
        loadBtn.setPreferredSize(new Dimension(110, 34));
        loadBtn.addActionListener(e -> refresh());

        JButton addBtn = UIComponents.secondaryButton("+ Ajouter");
        addBtn.addActionListener(e -> showForm(null));

        panel.add(new JLabel("Mois :"));
        panel.add(monthSpinner);
        panel.add(new JLabel("Annee :"));
        panel.add(yearSpinner);
        panel.add(loadBtn);
        panel.add(addBtn);
        return panel;
    }

    public void refresh() {
        int userId = SessionManager.getUserId();
        int month  = (int) monthSpinner.getValue();
        int year   = (int) yearSpinner.getValue();
        try {
            currentBudgets = ctrl.getBudgets(userId, month, year);
            budgetsPanel.removeAll();
            if (currentBudgets.isEmpty()) {
                JLabel empty = new JLabel("Aucun budget pour ce mois. Cliquez sur '+ Ajouter'.");
                empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                empty.setForeground(AppColors.TEXT_LIGHT);
                empty.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
                budgetsPanel.add(empty);
            } else {
                for (Budget b : currentBudgets) budgetsPanel.add(buildBudgetCard(b));
            }
            budgetsPanel.revalidate();
            budgetsPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildBudgetCard(Budget b) {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, AppColors.WHITE);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        double pct = b.getPercentage();
        Color statusColor = pct >= 100 ? AppColors.DANGER :
                            pct >= 90  ? AppColors.DANGER :
                            pct >= 70  ? AppColors.WARNING : AppColors.SUCCESS;

        // Left: category + amounts
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel catLabel = new JLabel(b.getCategoryName());
        catLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        catLabel.setForeground(AppColors.TEXT);

        JLabel amountsLabel = new JLabel(
            "Budget : " + FormatUtils.formatAmount(b.getAmountLimit()) +
            "  |  Dépensé : " + FormatUtils.formatAmount(b.getAmountSpent()) +
            "  |  Restant : " + FormatUtils.formatAmount(b.getRemaining()));
        amountsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        amountsLabel.setForeground(AppColors.TEXT_LIGHT);

        JProgressBar pb = UIComponents.styledProgressBar(pct);
        pb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));

        JLabel pctLabel = new JLabel(FormatUtils.formatPercentage(pct));
        pctLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pctLabel.setForeground(statusColor);

        left.add(catLabel);
        left.add(Box.createVerticalStrut(6));
        left.add(amountsLabel);
        left.add(Box.createVerticalStrut(8));
        left.add(pb);
        left.add(Box.createVerticalStrut(4));
        left.add(pctLabel);

        // Right: action buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        JButton editBtn = UIComponents.secondaryButton("Modifier");
        editBtn.setPreferredSize(new Dimension(80, 32));
        JButton delBtn  = UIComponents.dangerButton("Supprimer");
        delBtn.setPreferredSize(new Dimension(90, 32));

        editBtn.addActionListener(e -> showForm(b));
        delBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Supprimer ce budget ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try { ctrl.remove(b.getId(), SessionManager.getUserId()); refresh(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
            }
        });
        right.add(editBtn);
        right.add(delBtn);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        wrapper.add(card);
        return wrapper;
    }

    private void showForm(Budget existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Ajouter un budget" : "Modifier le budget", true);
        dialog.setSize(400, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppColors.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Category> catBox = new JComboBox<>();
        JTextField limitField = UIComponents.amountField(15);
        JSpinner mSpinner = new JSpinner(new SpinnerNumberModel((int) monthSpinner.getValue(), 1, 12, 1));
        JSpinner ySpinner = new JSpinner(new SpinnerNumberModel((int) yearSpinner.getValue(), 2020, 2100, 1));

        try {
            for (Category c : txCtrl.getCategoriesByType("EXPENSE")) catBox.addItem(c);
        } catch (Exception e) { /* ignore */ }

        if (existing != null) {
            limitField.setText(existing.getAmountLimit().toPlainString());
            for (int i = 0; i < catBox.getItemCount(); i++) {
                if (catBox.getItemAt(i).getId() == existing.getCategoryId()) { catBox.setSelectedIndex(i); break; }
            }
        }

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Categorie :"), gbc);
        gbc.gridx = 1; form.add(catBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Limite (Ar) :"), gbc);
        gbc.gridx = 1; form.add(limitField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Mois :"), gbc);
        gbc.gridx = 1; form.add(mSpinner, gbc);
        gbc.gridx = 0; gbc.gridy = 3; form.add(new JLabel("Annee :"), gbc);
        gbc.gridx = 1; form.add(ySpinner, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(AppColors.WHITE);
        JButton save   = UIComponents.primaryButton("Enregistrer");
        JButton cancel = UIComponents.secondaryButton("Annuler");
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            try {
                if (!ValidationUtils.isPositiveAmount(limitField.getText()))
                    throw new IllegalArgumentException("Limite invalide.");
                Budget b = existing != null ? existing : new Budget();
                b.setUserId(SessionManager.getUserId());
                b.setCategoryId(((Category) catBox.getSelectedItem()).getId());
                b.setAmountLimit(new BigDecimal(limitField.getText().trim()));
                b.setMonth((int) mSpinner.getValue());
                b.setYear((int) ySpinner.getValue());
                if (existing == null) ctrl.add(b); else ctrl.edit(b);
                dialog.dispose();
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(cancel);
        btnPanel.add(save);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
