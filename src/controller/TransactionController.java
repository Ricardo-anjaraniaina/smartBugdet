package controller;

import database.TransactionDAO;
import database.CategoryDAO;
import model.Transaction;
import model.Category;
import utils.ValidationUtils;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TransactionController {
    private final TransactionDAO dao = new TransactionDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    public List<Transaction> getAll(int userId) throws SQLException {
        return dao.findByUser(userId);
    }

    public List<Transaction> search(int userId, String keyword, String type,
                                     Integer categoryId, LocalDate from, LocalDate to) throws SQLException {
        return dao.search(userId, keyword, type, categoryId, from, to);
    }

    public List<Transaction> getRecent(int userId, int limit) throws SQLException {
        return dao.findRecent(userId, limit);
    }

    public void add(Transaction t) throws SQLException {
        validate(t);
        dao.create(t);
    }

    public void edit(Transaction t) throws SQLException {
        validate(t);
        dao.update(t);
    }

    public void remove(int id, int userId) throws SQLException {
        dao.delete(id, userId);
    }

    public List<Category> getCategories() throws SQLException {
        return categoryDAO.findAll();
    }

    public List<Category> getCategoriesByType(String type) throws SQLException {
        return categoryDAO.findByType(type);
    }

    private void validate(Transaction t) {
        if (!ValidationUtils.isValidAmount(t.getAmount()))
            throw new IllegalArgumentException("Le montant doit être positif.");
        if (ValidationUtils.isNullOrEmpty(t.getDescription()))
            throw new IllegalArgumentException("La description est obligatoire.");
        if (t.getTransactionDate() == null)
            throw new IllegalArgumentException("La date est obligatoire.");
    }
}
