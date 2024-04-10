package com.ncr.zatca.greencore.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class InvoiceLine {
    private long id;
    private Quantity invoicedQuantity;
    private Amount lineExtensionAmount;
    private Item item;
    private Price price;
    private List<AllowanceCharge> allowanceCharge = new ArrayList<AllowanceCharge>();

    public BigDecimal getSignedSumOfAllowances() {
        BigDecimal sum = BigDecimal.ZERO;
        for (AllowanceCharge singleAllowance : allowanceCharge) {
            BigDecimal sign = singleAllowance.isChargeIndicator() ? BigDecimal.ONE : BigDecimal.ONE.negate();

            sum = sum.add(singleAllowance.getAmount().getValue().multiply(sign));
        }
        return sum;
    }
}
