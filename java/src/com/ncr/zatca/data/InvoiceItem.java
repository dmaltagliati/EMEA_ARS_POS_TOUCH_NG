package com.ncr.zatca.data;

import com.ncr.zatca.ZatcaManager;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.ncr.FmtIo.roundBy;
import static com.ncr.Struc.vat;

@Data
public class InvoiceItem {
    private int id;
    private String text;
    private String number;
    private String additionalText;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal amount;
    private BigDecimal adjustment = BigDecimal.ZERO;
    private String measureUnit;
    private List<InvoiceDiscount> discounts = new ArrayList<InvoiceDiscount>();
    private InvoiceTax invoiceTax;
    private boolean negative;
    private boolean weightItem;

    public BigDecimal getSumOfDiscounts() {
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceDiscount discount : discounts) {
            sum = sum.add(discount.getAmount());
        }
        return sum;
    }

    public BigDecimal getNetSumOfDiscounts() {
        return getNet(getSumOfDiscounts(), 2);
    }

    public BigDecimal getNetAmount() {
        return getNet(amount, 2);
    }

    public BigDecimal getNetPrice() {
        return getNet(price, 2);
    }

    public BigDecimal getNetPrice(int decimals) {
        return getNet(price, decimals);
    }

    private BigDecimal getNet(BigDecimal amount, int decimals) {
        BigDecimal tax = amount
                .multiply(invoiceTax.getRate())
                .divide(new BigDecimal(100).add(invoiceTax.getRate()), decimals, RoundingMode.HALF_DOWN);
        return amount.subtract(tax);
    }

    public void fixTax() {
        long amt = amount.multiply(ZatcaManager.decimals).longValue();
        long rate = invoiceTax.getRate().longValue() * 10;
        long tax = roundBy(amt * rate * 10 / (1000 + rate), 10);
        invoiceTax.setAmount(new BigDecimal(tax).divide(ZatcaManager.decimals).abs());
        invoiceTax.setGross(new BigDecimal(amt).divide(ZatcaManager.decimals).abs());
    }
}
