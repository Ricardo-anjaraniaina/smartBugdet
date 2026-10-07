package database;

import model.SavingsGoal;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SavingsGoalDAO {

    private SavingsGoal map(ResultSet rs) throws SQLException {
        SavingsGoal g = new SavingsGoal();
        g.setId(rs.getInt("id"));
        g.setUserId(rs.getInt("user_id"));
        g.setName(rs.getString("name"));
        g.setTargetAmount(rs.getBigDecimal("target_amount"));
        g.setCurrentAmount(rs.getBigDecimal("current_amount"));
        Date d = rs.getDate("deadline");
        if (d != null) g.setDeadline(d.toLocalDate());
        return g;
    }

    public void create(SavingsGoal g) throws SQLException {
        String sql = "INSERT INTO savings_goals (user_id,name,target_amount,current_amount,deadline) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, g.getUserId());
            ps.setString(2, g.getName());
            ps.setBigDecimal(3, g.getTargetAmount());
            ps.setBigDecimal(4, g.getCurrentAmount());
            ps.setDate(5, g.getDeadline() != null ? Date.valueOf(g.getDeadline()) : null);
            ps.executeUpdate();
        }
    }

    public void update(SavingsGoal g) throws SQLException {
        String sql = "UPDATE savings_goals SET name=?,target_amount=?,current_amount=?,deadline=? WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, g.getName());
            ps.setBigDecimal(2, g.getTargetAmount());
            ps.setBigDecimal(3, g.getCurrentAmount());
            ps.setDate(4, g.getDeadline() != null ? Date.valueOf(g.getDeadline()) : null);
            ps.setInt(5, g.getId());
            ps.setInt(6, g.getUserId());
            ps.executeUpdate();
        }
    }

    public void delete(int id, int userId) throws SQLException {
        String sql = "DELETE FROM savings_goals WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public List<SavingsGoal> findByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM savings_goals WHERE user_id=? ORDER BY created_at DESC";
        List<SavingsGoal> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void addAmount(int id, int userId, BigDecimal amount) throws SQLException {
        String sql = "UPDATE savings_goals SET current_amount = LEAST(current_amount + ?, target_amount) WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, id);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }
}
