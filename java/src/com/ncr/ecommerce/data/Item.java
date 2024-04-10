package com.ncr.ecommerce.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Item {
    private String code;
    private BigDecimal price;
    private int qty;
    private BigDecimal unitPrice;
    private String barcode;
    private String description;

    public Item(String code, BigDecimal price, int qty, BigDecimal unitPrice, String barcode) {
        this.code = code;
        this.price = price;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.barcode = barcode;
    }
    public Item(String code, BigDecimal price) {
        this.code = code;
        this.price = price;
        qty = 1;
        this.barcode = code;
        description = "";
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }
}