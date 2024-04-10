package com.ncr.zatca.rbs.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RbsInvoiceDiscount {
    private boolean discountApplicable;
    private BigDecimal discountAmount;

}
