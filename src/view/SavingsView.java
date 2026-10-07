package view;

import controller.SavingsController;
import controller.SessionManager;
import model.SavingsGoal;
import utils.FormatUtils;
import utils.ValidationUtils;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class SavingsView extends JPanel {

    private final SavingsController ctrl = new SavingsController();
    private JPanel goalsPanel;

    public SavingsView() {
        setLayout(new BorderLayout());
        setBackground(AppColors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        JLabel title = new JLabel("Objectifs d'epargne");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(AppColors.TEXT);

        JButton addBtn = UIComponents.primaryButton("+ Nouvel objectif");
        addBtn.addActionListener(e -> showForm(null));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        goalsPanel = new JPanel();
        goalsPanel.setOpaque(false);
        goalsPanel.setLayout(new BoxLayout(goalsPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(goalsPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppColors.BACKGROUND);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        try {
            List<SavingsGoal> goals = ctrl.getGoals(SessionManager.getUserId());
            goalsPanel.removeAll();
            if (goals.isEmpty()) {
                JLabel empty = new JLabel("Aucun objectif. Cliquez sur '+ Nouvel objectif'.");
                empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                empty.setForeground(AppColors.TEXT_LIGHT);
                empty.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
                goalsPanel.add(empty);
            } else {
                for (SavingsGoal g : goals) goalsPanel.add(buildGoalCard(g));
            }
            goalsPanel.revalidate();
            goalsPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildGoalCard(SavingsGoal g) {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12, AppColors.WHITE);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        double pct = g.getProgressPercentage();

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(g.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(AppColors.TEXT);

        String deadlineStr = g.getDeadline() != null ? "  |  Echeance : " + FormatUtils.formatDate(g.getDeadline()) : "";
        JLabel amountsLabel = new JLabel(
            "Objectif : " + FormatUtils.formatAmount(g.getTargetAmount()) +
            "  |  Epargne : " + FormatUtils.formatAmount(g.getCurrentAmount()) +
            "  |  Restant : " + FormatUtils.formatAmount(g.getRemainingAmount()) + deadlineStr);
        amountsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        amountsLabel.setForeground(AppColors.TEXT_LIGHT);

        JProgressBar pb = UIComponents.styledProgressBar(pct);
        pb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));

        Color pctColor = pct >= 100 ? AppColors.SUCCESS : AppColors.PRIMARY;
        JLabel pctLabel = new JLabel(FormatUtils.formatPercentage(pct) + (pct >= 100 ? " [Atteint !]" : ""));
        pctLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pctLabel.setForeground(pctColor);

        left.add(nameLabel);
        left.add(Box.createVerticalStrut(6));
        left.add(amountsLabel);
        left.add(Box.createVerticalStrut(8));
        left.add(pb);
        left.add(Box.createVerticalStrut(4));
        left.add(pctLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        JButton depositBtn = UIComponents.primaryButton("+ Epargner");
        depositBtn.setPreferredSize(new Dimension(120, 32));
        JButton editBtn    = UIComponents.secondaryButton("Modifier");
        editBtn.setPreferredSize(new Dimension(90, 32));
        JButton delBtn     = UIComponents.dangerButton("Supprimer");
        delBtn.setPreferredSize(new Dimension(100, 32));

        depositBtn.addActionListener(e -> showDepositDialog(g));
        editBtn.addActionListener(e -> showForm(g));
        delBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cet objectif ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try { ctrl.remove(g.getId(), SessionManager.getUserId()); refresh(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
            }
        });

        right.add(depositBtn);
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

    private void showDepositDialog(SavingsGoal g) {
        String input = JOptionPane.showInputDialog(this,
            "Montant à ajouter à \"" + g.getName() + "\" :", "Épargner", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.isBlank()) return;
        try {
            if (!ValidationUtils.isPositiveAmount(input))
                throw new IllegalArgumentException("Montant invalide.");
            ctrl.deposit(g.getId(), SessionManager.getUserId(), new BigDecimal(input.trim()));
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showForm(SavingsGoal existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Nouvel objectif" : "Modifier l'objectif", true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppColors.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField    = UIComponents.styledField(20);
        JTextField targetField  = UIComponents.amountField(20);
        JTextField currentField = UIComponents.amountField(20);
        DatePickerField deadlineField = new DatePickerField();

        if (existing != null) {
            nameField.setText(existing.getName());
            targetField.setText(existing.getTargetAmount().toPlainString());
            currentField.setText(existing.getCurrentAmount().toPlainString());
            if (existing.getDeadline() != null) deadlineField.setDate(existing.getDeadline());
        }

        addRow(form, gbc, 0, "Nom :", nameField);
        addRow(form, gbc, 1, "Objectif (Ar) :", targetField);
        addRow(form, gbc, 2, "Epargne actuelle :", currentField);
        addRow(form, gbc, 3, "Echeance :", deadlineField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(AppColors.WHITE);
        JButton save   = UIComponents.primaryButton("Enregistrer");
        JButton cancel = UIComponents.secondaryButton("Annuler");
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            try {
                if (ValidationUtils.isNullOrEmpty(nameField.getText()))
                    throw new IllegalArgumentException("Le nom est obligatoire.");
                if (!ValidationUtils.isPositiveAmount(targetField.getText()))
                    throw new IllegalArgumentException("Objectif invalide.");

                SavingsGoal g = existing != null ? existing : new SavingsGoal();
                g.setUserId(SessionManager.getUserId());
                g.setName(nameField.getText().trim());
                g.setTargetAmount(new BigDecimal(targetField.getText().trim()));
                String cur = currentField.getText().trim();
                g.setCurrentAmount(cur.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cur));
                String dl = deadlineField.getText().trim();
                g.setDeadline(dl.isEmpty() ? null : deadlineField.getDate());

                if (existing == null) ctrl.add(g); else ctrl.edit(g);
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

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(field, gbc);
    }
}
