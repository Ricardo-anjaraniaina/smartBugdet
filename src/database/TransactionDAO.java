package database;

import model.Transaction;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private Transaction map(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setCategoryId(rs.getInt("category_id"));
        t.setCategoryName(rs.getString("category_name"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setType(rs.getString("type"));
        t.setDescription(rs.getString("description"));
        t.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
        t.setPaymentMethod(rs.getString("payment_method"));
        return t;
    }

    public void create(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (user_id,category_id,amount,type,description,transaction_date,payment_method) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getUserId());
            ps.setInt(2, t.getCategoryId());
            ps.setBigDecimal(3, t.getAmount());
            ps.setString(4, t.getType());
            ps.setString(5, t.getDescription());
            ps.setDate(6, Date.valueOf(t.getTransactionDate()));
            ps.setString(7, t.getPaymentMethod());
            ps.executeUpdate();
        }
    }

    public void update(Transaction t) throws SQLException {
        String sql = "UPDATE transactions SET category_id=?,amount=?,type=?,description=?,transaction_date=?,payment_method=? WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getCategoryId());
            ps.setBigDecimal(2, t.getAmount());
            ps.setString(3, t.getType());
            ps.setString(4, t.getDescription());
            ps.setDate(5, Date.valueOf(t.getTransactionDate()));
            ps.setString(6, t.getPaymentMethod());
            ps.setInt(7, t.getId());
            ps.setInt(8, t.getUserId());
            ps.executeUpdate();
        }
    }

    public void delete(int id, int userId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public List<Transaction> findByUser(int userId) throws SQLException {
        String sql = """
            SELECT t.*, c.name AS category_name
            FROM transactions t JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ?
            ORDER BY t.transaction_date DESC, t.created_at DESC
            """;
        return query(sql, userId);
    }

    public List<Transaction> findRecent(int userId, int limit) throws SQLException {
        String sql = """
            SELECT t.*, c.name AS category_name
            FROM transactions t JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ?
            ORDER BY t.transaction_date DESC, t.created_at DESC
            LIMIT ?
            """;
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Transaction> search(int userId, String keyword, String type, Integer categoryId,
                                     LocalDate from, LocalDate to) throws SQLException {
        StringBuilder sb = new StringBuilder("""
            SELECT t.*, c.name AS category_name
            FROM transactions t JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ?
            """);
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (keyword != null && !keyword.isBlank()) {
            sb.append(" AND (t.description LIKE ? OR c.name LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (type != null && !type.equals("Tous")) {
            sb.append(" AND t.type = ?");
            params.add(type.equals("REVENU") ? "INCOME" : type);
        }
        if (categoryId != null && categoryId > 0) {
            sb.append(" AND t.category_id = ?");
            params.add(categoryId);
        }
        if (from != null) {
            sb.append(" AND t.transaction_date >= ?");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sb.append(" AND t.transaction_date <= ?");
            params.add(Date.valueOf(to));
        }
        sb.append(" ORDER BY t.transaction_date DESC");

        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public BigDecimal sumByType(int userId, String type) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE user_id=? AND type=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    public BigDecimal sumByTypeAndMonth(int userId, String type, int month, int year) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE user_id=? AND type=? AND MONTH(transaction_date)=? AND YEAR(transaction_date)=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setInt(3, month);
            ps.setInt(4, year);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    public BigDecimal sumByCategoryAndMonth(int userId, int categoryId, int month, int year) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE user_id=? AND category_id=? AND type='EXPENSE' AND MONTH(transaction_date)=? AND YEAR(transaction_date)=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setInt(3, month);
            ps.setInt(4, year);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private List<Transaction> query(String sql, int userId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }
}
