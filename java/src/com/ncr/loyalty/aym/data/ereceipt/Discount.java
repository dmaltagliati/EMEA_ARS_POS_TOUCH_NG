package com.ncr.loyalty.aym.data.ereceipt;

import java.math.BigDecimal;

public class Discount {
    private String description;
    private BigDecimal amount;

    public Discount() {
    }

    public Discount(String description, BigDecimal amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Discount{" +
                "description='" + description + '\'' +
                ", amount=" + amount +
                '}';
    }
}
