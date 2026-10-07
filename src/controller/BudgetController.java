package controller;

import database.BudgetDAO;
import database.TransactionDAO;
import model.Budget;
import utils.ValidationUtils;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class BudgetController {
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public List<Budget> getBudgets(int userId, int month, int year) throws SQLException {
        List<Budget> budgets = budgetDAO.findByUserAndMonth(userId, month, year);
        for (Budget b : budgets) {
            BigDecimal spent = transactionDAO.sumByCategoryAndMonth(userId, b.getCategoryId(), month, year);
            b.setAmountSpent(spent);
        }
        return budgets;
    }

    public void add(Budget b) throws SQLException {
        if (!ValidationUtils.isValidAmount(b.getAmountLimit()))
            throw new IllegalArgumentException("Le budget doit être positif.");
        budgetDAO.create(b);
    }

    public void edit(Budget b) throws SQLException {
        if (!ValidationUtils.isValidAmount(b.getAmountLimit()))
            throw new IllegalArgumentException("Le budget doit être positif.");
        budgetDAO.update(b);
    }

    public void remove(int id, int userId) throws SQLException {
        budgetDAO.delete(id, userId);
    }
}
