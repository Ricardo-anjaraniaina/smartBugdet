package view;

import controller.SessionManager;
import controller.TransactionController;
import model.Category;
import model.Transaction;
import utils.FormatUtils;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionsView extends JPanel {

    private final TransactionController ctrl = new TransactionController();

    private DefaultTableModel tableModel;
    private JTable table;
    private List<Transaction> currentList;

    // Filter fields
    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JComboBox<Category> categoryFilter;
    private DatePickerField dateFromField, dateToField;

    public TransactionsView() {
        setLayout(new BorderLayout());
        setBackground(AppColors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        JLabel title = new JLabel("Transactions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(AppColors.TEXT);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        center.add(buildFilters(), BorderLayout.NORTH);
        center.add(buildTable(), BorderLayout.CENTER);
        center.add(buildButtons(), BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
    }

    private JPanel buildFilters() {
        UIComponents.RoundedPanel panel = new UIComponents.RoundedPanel(12, AppColors.WHITE);
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));

        searchField = UIComponents.styledField(18);
        searchField.setToolTipText("Rechercher...");

        typeFilter = new JComboBox<>(new String[]{"Tous", "INCOME", "EXPENSE"});
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem(new Category(0, "Toutes", ""));

        dateFromField = new DatePickerField();
        dateToField   = new DatePickerField();

        JButton searchBtn = UIComponents.primaryButton("Rechercher");
        searchBtn.setPreferredSize(new Dimension(140, 34));
        searchBtn.addActionListener(e -> applyFilters());

        JButton resetBtn = UIComponents.secondaryButton("Réinitialiser");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            typeFilter.setSelectedIndex(0);
            categoryFilter.setSelectedIndex(0);
            dateFromField.setText("");
            dateToField.setText("");
            refresh();
        });

        panel.add(new JLabel("Recherche :"));
        panel.add(searchField);
        panel.add(new JLabel("Type:"));
        panel.add(typeFilter);
        panel.add(new JLabel("Categorie:"));
        panel.add(categoryFilter);
        panel.add(new JLabel("Du:"));
        panel.add(dateFromField);
        panel.add(new JLabel("Au:"));
        panel.add(dateToField);
        panel.add(searchBtn);
        panel.add(resetBtn);
        return panel;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Date", "Description", "Catégorie", "Type", "Montant", "Paiement"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));
        scroll.getViewport().setBackground(AppColors.WHITE);
        return scroll;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

        JButton addBtn  = UIComponents.primaryButton("+ Ajouter");
        JButton editBtn = UIComponents.secondaryButton("Modifier");
        JButton delBtn  = UIComponents.dangerButton("Supprimer");

        addBtn.addActionListener(e  -> showForm(null));
        editBtn.addActionListener(e -> {
            Transaction t = getSelected();
            if (t != null) showForm(t);
        });
        delBtn.addActionListener(e  -> deleteSelected());

        panel.add(addBtn);
        panel.add(editBtn);
        panel.add(delBtn);
        return panel;
    }

    private void applyFilters() {
        String keyword = searchField.getText().trim();
        String type = (String) typeFilter.getSelectedItem();
        Category cat = (Category) categoryFilter.getSelectedItem();
        Integer catId = (cat != null && cat.getId() > 0) ? cat.getId() : null;

        LocalDate from = dateFromField.getDate();
        LocalDate to   = dateToField.getDate();

        try {
            currentList = ctrl.search(SessionManager.getUserId(), keyword,
                "Tous".equals(type) ? null : type, catId, from, to);
            populateTable(currentList);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void refresh() {
        try {
            // Reload categories
            categoryFilter.removeAllItems();
            categoryFilter.addItem(new Category(0, "Toutes", ""));
            for (Category c : ctrl.getCategories()) categoryFilter.addItem(c);

            currentList = ctrl.getAll(SessionManager.getUserId());
            populateTable(currentList);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Transaction> list) {
        tableModel.setRowCount(0);
        for (Transaction t : list) {
            tableModel.addRow(new Object[]{
                t.getId(),
                FormatUtils.formatDate(t.getTransactionDate()),
                t.getDescription(),
                t.getCategoryName(),
                "INCOME".equals(t.getType()) ? "Revenu" : "Dépense",
                FormatUtils.formatAmount(t.getAmount()),
                t.getPaymentMethod()
            });
        }
    }

    private Transaction getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez une transaction."); return null; }
        int id = (int) tableModel.getValueAt(row, 0);
        return currentList.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
    }

    private void deleteSelected() {
        Transaction t = getSelected();
        if (t == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer cette transaction ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ctrl.remove(t.getId(), SessionManager.getUserId());
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showForm(Transaction existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Ajouter une transaction" : "Modifier la transaction", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(460, 420);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppColors.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField descField   = UIComponents.styledField(20);
        JTextField amountField = UIComponents.amountField(20);
        DatePickerField dateField = new DatePickerField();

        JComboBox<String> typeBox = new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        JComboBox<Category> catBox = new JComboBox<>();
        JComboBox<String> payBox = new JComboBox<>(
            new String[]{"Espèces", "Carte bancaire", "Mobile Money", "Virement", "Autre"});

        try {
            for (Category c : ctrl.getCategories()) catBox.addItem(c);
        } catch (Exception e) { /* ignore */ }

        if (existing != null) {
            descField.setText(existing.getDescription());
            amountField.setText(existing.getAmount().toPlainString());
            dateField.setDate(existing.getTransactionDate());
            typeBox.setSelectedItem(existing.getType());
            for (int i = 0; i < catBox.getItemCount(); i++) {
                if (catBox.getItemAt(i).getId() == existing.getCategoryId()) {
                    catBox.setSelectedIndex(i); break;
                }
            }
            if (existing.getPaymentMethod() != null) payBox.setSelectedItem(existing.getPaymentMethod());
        } else {
            dateField.setDate(LocalDate.now());
        }

        addFormRow(form, gbc, 0, "Description :", descField);
        addFormRow(form, gbc, 1, "Montant (Ar) :", amountField);
        addFormRow(form, gbc, 2, "Type :", typeBox);
        addFormRow(form, gbc, 3, "Categorie :", catBox);
        addFormRow(form, gbc, 4, "Date :", dateField);
        addFormRow(form, gbc, 5, "Paiement :", payBox);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(AppColors.WHITE);
        JButton save   = UIComponents.primaryButton("Enregistrer");
        JButton cancel = UIComponents.secondaryButton("Annuler");
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            try {
                if (ValidationUtils.isNullOrEmpty(descField.getText()))
                    throw new IllegalArgumentException("La description est obligatoire.");
                if (!ValidationUtils.isPositiveAmount(amountField.getText()))
                    throw new IllegalArgumentException("Montant invalide.");

                Transaction t = existing != null ? existing : new Transaction();
                t.setUserId(SessionManager.getUserId());
                t.setDescription(descField.getText().trim());
                t.setAmount(new BigDecimal(amountField.getText().trim()));
                t.setType((String) typeBox.getSelectedItem());
                t.setCategoryId(((Category) catBox.getSelectedItem()).getId());
                LocalDate d = dateField.getDate();
                if (d == null) throw new IllegalArgumentException("Date invalide.");
                t.setTransactionDate(d);
                t.setPaymentMethod((String) payBox.getSelectedItem());

                if (existing == null) ctrl.add(t); else ctrl.edit(t);
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

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(field, gbc);
    }
}
