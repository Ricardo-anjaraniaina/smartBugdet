package database;

import model.Budget;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {

    private Budget map(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setCategoryName(rs.getString("category_name"));
        b.setAmountLimit(rs.getBigDecimal("amount_limit"));
        b.setMonth(rs.getInt("month"));
        b.setYear(rs.getInt("year"));
        return b;
    }

    public void create(Budget b) throws SQLException {
        String sql = "INSERT INTO budgets (user_id,category_id,amount_limit,month,year) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, b.getUserId());
            ps.setInt(2, b.getCategoryId());
            ps.setBigDecimal(3, b.getAmountLimit());
            ps.setInt(4, b.getMonth());
            ps.setInt(5, b.getYear());
            ps.executeUpdate();
        }
    }

    public void update(Budget b) throws SQLException {
        String sql = "UPDATE budgets SET amount_limit=? WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, b.getAmountLimit());
            ps.setInt(2, b.getId());
            ps.setInt(3, b.getUserId());
            ps.executeUpdate();
        }
    }

    public void delete(int id, int userId) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public List<Budget> findByUserAndMonth(int userId, int month, int year) throws SQLException {
        String sql = """
            SELECT b.*, c.name AS category_name
            FROM budgets b JOIN categories c ON b.category_id = c.id
            WHERE b.user_id=? AND b.month=? AND b.year=?
            ORDER BY c.name
            """;
        List<Budget> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }
}
