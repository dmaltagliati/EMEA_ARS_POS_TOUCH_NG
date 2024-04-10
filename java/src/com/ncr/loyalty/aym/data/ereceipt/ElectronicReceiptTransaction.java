package com.ncr.loyalty.aym.data.ereceipt;

import com.google.gson.annotations.SerializedName;
import com.ncr.common.data.AdditionalInfo;
import com.ncr.loyalty.aym.data.Variable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ElectronicReceiptTransaction {
    private String accountIdValue;
    private String store;
    private String locationCode;
    private String invoiceId;
    private String taxInvoiceId;
    private BigDecimal value;
    private Date timestamp;
    private Milestone milestone;
    private String initiatedBy;
    private String terminalId;
    private String returnCode;
    @SerializedName("Zatca-qr")
    private String zatcaQr;
    private List<TransactionPromotion> promotionsList;
    private List<TransactionProduct> transactionProductList;
    private List<PaymentDetail> paymentDetails;
    private List<TaxDetail> taxDetails;
    private List<Variable> transactionVariables;
    private String transactionUniqueReference;

    public ElectronicReceiptTransaction() {
        promotionsList = new ArrayList<TransactionPromotion>();
        transactionProductList = new ArrayList<TransactionProduct>();
        paymentDetails = new ArrayList<PaymentDetail>();
        taxDetails = new ArrayList<TaxDetail>();
        transactionVariables = new ArrayList<Variable>();
    }

    public String getAccountIdValue() {
        return accountIdValue;
    }

    public void setAccountIdValue(String accountIdValue) {
        this.accountIdValue = accountIdValue;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getTaxInvoiceId() {
        return taxInvoiceId;
    }

    public void setTaxInvoiceId(String taxInvoiceId) {
        this.taxInvoiceId = taxInvoiceId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<TransactionPromotion> getTransactionPromotionsList() {
        return promotionsList;
    }

    public void setTransactionPromotionsList(List<TransactionPromotion> promotionsList) {
        this.promotionsList = promotionsList;
    }

    public List<TransactionProduct> getTransactionProductList() {
        return transactionProductList;
    }

    public void setTransactionProductList(List<TransactionProduct> transactionTransactionProductList) {
        this.transactionProductList = transactionTransactionProductList;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getZatcaQr() {
        return zatcaQr;
    }

    public void setZatcaQr(String zatcaQr) {
        this.zatcaQr = zatcaQr;
    }

    public List<PaymentDetail> getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(List<PaymentDetail> paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public List<TransactionPromotion> getPromotionsList() {
        return promotionsList;
    }

    public void setPromotionsList(List<TransactionPromotion> promotionsList) {
        this.promotionsList = promotionsList;
    }

    public List<TaxDetail> getTaxDetails() {
        return taxDetails;
    }

    public void setTaxDetails(List<TaxDetail> taxDetails) {
        this.taxDetails = taxDetails;
    }

    public List<Variable> getTransactionVariables() {
        return transactionVariables;
    }

    public void setTransactionVariables(List<Variable> transactionVariables) {
        this.transactionVariables = transactionVariables;
    }

    public String getTransactionUniqueReference() {
        return transactionUniqueReference;
    }

    public void setTransactionUniqueReference(String transactionUniqueReference) {
        this.transactionUniqueReference = transactionUniqueReference;
    }

    @Override
    public String toString() {
        return "ElectronicReceiptTransaction{" +
                "accountIdValue='" + accountIdValue + '\'' +
                ", store='" + store + '\'' +
                ", locationCode='" + locationCode + '\'' +
                ", invoiceId='" + invoiceId + '\'' +
                ", taxInvoiceId='" + taxInvoiceId + '\'' +
                ", value=" + value +
                ", timestamp=" + timestamp +
                ", milestone=" + milestone +
                ", initiatedBy='" + initiatedBy + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", returnCode='" + returnCode + '\'' +
                ", zatcaQr='" + zatcaQr + '\'' +
                ", promotionsList=" + promotionsList +
                ", transactionProductList=" + transactionProductList +
                ", paymentDetails=" + paymentDetails +
                ", taxDetails=" + taxDetails +
                ", transactionVariables=" + transactionVariables +
                ", transactionUniqueReference='" + transactionUniqueReference + '\'' +
                '}';
    }
}
