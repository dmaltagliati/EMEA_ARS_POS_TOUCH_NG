package com.ncr.zatca.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceTax {
    private int id;
    private BigDecimal rate;
    private String text;
    private BigDecimal amount;
    private BigDecimal gross;

    public BigDecimal getTaxable() {
        return gross.subtract(amount);
    }
}
