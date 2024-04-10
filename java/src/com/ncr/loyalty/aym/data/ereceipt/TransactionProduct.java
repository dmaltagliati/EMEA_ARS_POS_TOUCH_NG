package com.ncr.loyalty.aym.data.ereceipt;

import com.google.gson.annotations.SerializedName;
import com.ncr.loyalty.aym.data.Variable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionProduct {
    private int transactionProductsId;
    private int transactionId;
    private String sku;
    private int productId;
    private String name;
    @SerializedName("Name-Ar")
    private String additionalName;
    private int productItmQty;
    private BigDecimal totalValue;
    private Date timestamp;
    private List<Variable> productVariables;
    private BigDecimal price;
    private Discount discount;

    public TransactionProduct() {
        productVariables = new ArrayList<Variable>();
    }

    public TransactionProduct(int transactionProductsId, int transactionId, String sku, int productId, String name, String additionalName, int productItmQty, BigDecimal totalValue, Date timestamp, BigDecimal price) {
        this.transactionProductsId = transactionProductsId;
        this.transactionId = transactionId;
        this.sku = sku;
        this.productId = productId;
        this.name = name;
        this.additionalName = additionalName;
        this.productItmQty = productItmQty;
        this.totalValue = totalValue;
        this.timestamp = timestamp;
        this.price = price;
        productVariables = new ArrayList<Variable>();
        discount = new Discount();
    }

    public int getTransactionProductsId() {
        return transactionProductsId;
    }

    public void setTransactionProductsId(int transactionProductsId) {
        this.transactionProductsId = transactionProductsId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdditionalName() {
        return additionalName;
    }

    public void setAdditionalName(String additionalName) {
        this.additionalName = additionalName;
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

    public List<Variable> getProductVariables() {
        return productVariables;
    }

    public void setProductVariables(List<Variable> productVariables) {
        this.productVariables = productVariables;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    @Override
    public String toString() {
        return "TransactionProduct{" +
                "transactionProductsId=" + transactionProductsId +
                ", transactionId=" + transactionId +
                ", sku='" + sku + '\'' +
                ", productId=" + productId +
                ", name='" + name + '\'' +
                ", additionalName='" + additionalName + '\'' +
                ", productItmQty=" + productItmQty +
                ", totalValue=" + totalValue +
                ", timestamp=" + timestamp +
                ", productVariables=" + productVariables +
                ", price=" + price +
                ", discount=" + discount +
                '}';
    }
}
