package model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String password;
    private java.math.BigDecimal initialBalance = java.math.BigDecimal.ZERO;
    private LocalDateTime createdAt;

    public User() {}

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public java.math.BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(java.math.BigDecimal initialBalance) { this.initialBalance = initialBalance != null ? initialBalance : java.math.BigDecimal.ZERO; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return username; }
}
