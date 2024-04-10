package com.ncr.zatca.rbs.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class RbsInvoiceData {
    public static final String TYPE_SIMPLIFIED_INVOICE = "SIMPLIFIEDINVOICE";
    public static final String TYPE_SIMPLIFIED_CREDIT_NOTE = "CREDITSIMPLIFIEDINVOICE";
    public static final String TYPE_SIMPLIFIED_DEBIT_NOTE = "DEBITSIMPLIFIEDINVOICE";

    public static final String PAYMENT_TYPE_CASH = "CASH";
    public static final String PAYMENT_TYPE_CREDIT = "CREDIT";
    public static final String PAYMENT_TYPE_BANK = "PAYMENT_TO_BANK_ACCOUNT";
    public static final String PAYMENT_TYPE_CARD = "BANKCARD";

    private String invoiceID;
    private String invoiceRef;
    private String invoiceRefDate;
    private String invoiceType;
    private List<RbsInvoiceItem> invoiceItemList = new ArrayList<RbsInvoiceItem>();
    private BigDecimal invoiceTotalAmountWithVat;
    private BigDecimal invoiceTotalAmountWithOutVat;
    private BigDecimal invoiceTotalVatAmount;
    private Date invoiceDateTimeIssue;
    private String invoicePaymentType;
    private RbsInvoiceDiscount documentDiscount;
    private List<RbsInvoiceTender> tenders = new ArrayList<RbsInvoiceTender>();
}
