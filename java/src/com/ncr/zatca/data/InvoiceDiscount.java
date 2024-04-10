package com.ncr.zatca.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class InvoiceDiscount {
    private String text;
    private BigDecimal amount;
    List<Integer> ids = new ArrayList<Integer>();

    public InvoiceDiscount(String text, BigDecimal amount) {
        this.text = text;
        this.amount = amount;
    }

    public InvoiceDiscount(String text, BigDecimal amount, int id) {
        this.text = text;
        this.amount = amount;
        ids.add(id);
    }
}
