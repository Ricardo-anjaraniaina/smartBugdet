package controller;

import database.SavingsGoalDAO;
import model.SavingsGoal;
import utils.ValidationUtils;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class SavingsController {
    private final SavingsGoalDAO dao = new SavingsGoalDAO();

    public List<SavingsGoal> getGoals(int userId) throws SQLException {
        return dao.findByUser(userId);
    }

    public void add(SavingsGoal g) throws SQLException {
        if (!ValidationUtils.isValidAmount(g.getTargetAmount()))
            throw new IllegalArgumentException("L'objectif doit être positif.");
        if (ValidationUtils.isNullOrEmpty(g.getName()))
            throw new IllegalArgumentException("Le nom est obligatoire.");
        dao.create(g);
    }

    public void edit(SavingsGoal g) throws SQLException {
        if (!ValidationUtils.isValidAmount(g.getTargetAmount()))
            throw new IllegalArgumentException("L'objectif doit être positif.");
        dao.update(g);
    }

    public void remove(int id, int userId) throws SQLException {
        dao.delete(id, userId);
    }

    public void deposit(int id, int userId, BigDecimal amount) throws SQLException {
        if (!ValidationUtils.isValidAmount(amount))
            throw new IllegalArgumentException("Le montant doit être positif.");
        dao.addAmount(id, userId, amount);
    }
}
