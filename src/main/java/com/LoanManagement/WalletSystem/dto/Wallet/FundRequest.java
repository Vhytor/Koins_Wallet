package com.LoanManagement.WalletSystem.dto.Wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class FundRequest {
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private String reference;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}

