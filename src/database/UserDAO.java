package database;

import model.User;
import utils.PasswordUtils;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

public class UserDAO {

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, initial_balance FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
                    user.setInitialBalance(rs.getBigDecimal("initial_balance"));
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> authenticate(String username, String rawPassword) throws SQLException {
        Optional<User> opt = findByUsername(username);
        if (opt.isPresent() && PasswordUtils.verify(rawPassword, opt.get().getPassword())) {
            return opt;
        }
        return Optional.empty();
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void register(String username, String hashedPassword) throws SQLException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.executeUpdate();
        }
    }

    public void updateInitialBalance(int userId, BigDecimal amount) throws SQLException {
        String sql = "UPDATE users SET initial_balance = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public BigDecimal getInitialBalance(int userId) throws SQLException {
        String sql = "SELECT initial_balance FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("initial_balance");
            }
        }
        return BigDecimal.ZERO;
    }
}
