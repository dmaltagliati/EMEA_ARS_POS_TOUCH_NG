package com.ncr.loyalty.aym.data;

import java.math.BigDecimal;
import java.util.Date;

public class TransactionItem {
    private String sku;
    private int productItmQty;
    private BigDecimal totalValue;
    private Date timestamp;

    public TransactionItem() {
    }

    public TransactionItem(String sku, int productItmQty, BigDecimal totalValue, Date timestamp) {
        this.sku = sku;
        this.productItmQty = productItmQty;
        this.totalValue = totalValue;
        this.timestamp = timestamp;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getProductItmQty() {
        return productItmQty;
    }

    public void setProductItmQty(int productItmQty) {
        this.productItmQty = productItmQty;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
