package com.ncr.zatca.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class InvoiceData {
    private String invoiceId;
    private String invoiceReferenceId;
    private String invoiceReferenceDate;
    private InvoiceType type;
    private List<InvoiceItem> items = new ArrayList<InvoiceItem>();
    private List<InvoiceDiscount> discounts = new ArrayList<InvoiceDiscount>();
    private List<InvoiceTender> tenders = new ArrayList<InvoiceTender>();
    private List<InvoiceTax> taxes = new ArrayList<InvoiceTax>();
    private List<String> journal = new ArrayList<String>();
    private List<String> transactionLog = new ArrayList<String>();

    public BigDecimal getNetAmount() {
        return getNetSumOfItemAmounts().subtract(getInvoiceSumOfDiscounts());
    }

    public BigDecimal getNetSumOfItemAmounts() {
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceItem item : items) {
            sum = sum.add(item.getNetAmount().subtract(item.getNetSumOfDiscounts()));
        }
        return sum;
    }

    public BigDecimal getInvoiceSumOfDiscounts() {
        return getInvoiceSumOfDiscounts(null);
    }

    public BigDecimal getInvoiceSumOfDiscounts(InvoiceTax invoiceTax) {
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceDiscount discount : discounts) {
            sum = sum.add(discount.getAmount().abs());
        }
        return sum;
    }

    public BigDecimal getInvoiceSumOfDiscounts(int sign) {
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceDiscount discount : discounts) {
            if (discount.getAmount().compareTo(BigDecimal.ZERO) == sign)
                sum = sum.add(discount.getAmount().abs());
        }
        return sum;
    }

    public BigDecimal getVatAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceTax tax : taxes) {
            sum = sum.add(tax.getAmount());
        }
        return sum;
    }

    public BigDecimal getGrossAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceTax tax : taxes) {
            sum = sum.add(tax.getGross());
        }
        return sum;
    }

    private BigDecimal getSumOfTenderType(String type) {
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceTender invoiceTender : tenders) {
            if (type.equals(invoiceTender.getType())) {
                sum = sum.add(invoiceTender.getAmount());
            }
        }
        return sum;
    }

    public BigDecimal getSumOfPayableTenders() {
        return getSumOfTenderType("Payable");
    }

    public BigDecimal getSumOfPrepaidTenders() {
        return getSumOfTenderType("Prepaid");
    }
}
