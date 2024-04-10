package com.ncr.loyalty.transaction;

import com.ncr.loyalty.aym.data.Variable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Item {
    private String code;
    private int quantity;
    private BigDecimal amount;
    private Date timestamp;
    private String description;
    private String additionalDescription;
    private String internal;
    private BigDecimal price;
    private int index;
    private LoyaltyDiscount discount;
    private List<Variable> variables;

    public Item(String code, int quantity, BigDecimal amount, Date timestamp, String description, String additionalDescription, String internal, BigDecimal price, LoyaltyDiscount discount, int index) {
        this.code = code;
        this.quantity = quantity;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
        this.additionalDescription = additionalDescription;
        this.internal = internal;
        this.price = price;
        this.discount = discount;
        this.index = index;
        variables = new ArrayList<Variable>();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdditionalDescription() {
        return additionalDescription;
    }

    public void setAdditionalDescription(String additionalDescription) {
        this.additionalDescription = additionalDescription;
    }

    public String getInternal() {
        return internal;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LoyaltyDiscount getDiscount() {
        return discount;
    }

    public void setDiscount(LoyaltyDiscount discount) {
        this.discount = discount;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "Item{" +
                "code='" + code + '\'' +
                ", quantity=" + quantity +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                ", additionalDescription='" + additionalDescription + '\'' +
                ", internal='" + internal + '\'' +
                ", price=" + price +
                ", index=" + index +
                ", discount=" + discount +
                ", variables=" + variables +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item that = (Item) o;
        return code.equals(that.code) &&
                quantity == that.quantity &&
                amount.equals(that.amount);
    }
}
