package model;

import java.math.BigDecimal;

public class Budget {
    private int id;
    private int userId;
    private int categoryId;
    private String categoryName;
    private BigDecimal amountLimit;
    private BigDecimal amountSpent; // calculated, not stored
    private int month;
    private int year;

    public Budget() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getAmountLimit() { return amountLimit; }
    public void setAmountLimit(BigDecimal amountLimit) { this.amountLimit = amountLimit; }

    public BigDecimal getAmountSpent() { return amountSpent != null ? amountSpent : BigDecimal.ZERO; }
    public void setAmountSpent(BigDecimal amountSpent) { this.amountSpent = amountSpent; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public BigDecimal getRemaining() {
        return amountLimit.subtract(getAmountSpent());
    }

    public double getPercentage() {
        if (amountLimit.compareTo(BigDecimal.ZERO) == 0) return 0;
        return getAmountSpent().divide(amountLimit, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }
}
