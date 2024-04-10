package com.ncr.zatca.greencore.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Invoice {
    private String id;
    private String issueDate;
    private String issueTime;
    private InvoiceType invoiceTypeCode;
    private BillingReference billingReference;
    private List<AllowanceCharge> allowanceCharge = new ArrayList<AllowanceCharge>();
    private MonetaryTotals legalMonetaryTotal;
    private List<TaxTotal> taxTotal = new ArrayList<TaxTotal>();
    private List<InvoiceLine> invoiceLine = new ArrayList<InvoiceLine>();
    private List<PaymentMeans> paymentMeans = new ArrayList<PaymentMeans>();
    private List<Delivery> delivery;
}
