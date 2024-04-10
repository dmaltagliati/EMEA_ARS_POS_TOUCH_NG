package com.ncr.zatca.rbs.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RbsInvoiceItem {
    private String itemID;
    private String itemTitle;
    private BigDecimal itemQuantity;
    private BigDecimal itemPrice;
    private String itemDescription;
    private BigDecimal itemTaxAmount;
    private String itemUnit;
    private RbsInvoiceDiscount itemDiscount;
}
