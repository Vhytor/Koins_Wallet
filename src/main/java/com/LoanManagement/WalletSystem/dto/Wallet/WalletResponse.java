package com.LoanManagement.WalletSystem.dto.Wallet;

import java.math.BigDecimal;
import java.time.Instant;

public class WalletResponse {
    private String id;
    private String userId;
    private BigDecimal balance;
    private String currency;
    private Integer status;
    private Instant createdAt;

    public WalletResponse() {}

    public WalletResponse(String id, String userId, BigDecimal balance, String currency, Integer status, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

