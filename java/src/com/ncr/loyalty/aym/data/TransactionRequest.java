package com.ncr.loyalty.aym.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionRequest extends PostRequest {
    private String accountIdValue;
    private String programCode;
    private String locationCode;
    private String transactionTypeCode;
    private String transactionUniqueReference;
    private BigDecimal value;
    private Date timestamp;
    private List<TransactionItem> transactionProductList;
    private String initiatedBy;
    private String terminalId;

    public TransactionRequest() {
        transactionProductList = new ArrayList<TransactionItem>();
    }

    public TransactionRequest(String accountIdValue, String programCode, String locationCode, String transactionTypeCode, String transactionUniqueReference, BigDecimal value, Date timestamp, List<TransactionItem> transactionProductList, String initiatedBy, String terminalId) {
        this.accountIdValue = accountIdValue;
        this.programCode = programCode;
        this.locationCode = locationCode;
        this.transactionTypeCode = transactionTypeCode;
        this.transactionUniqueReference = transactionUniqueReference;
        this.value = value;
        this.timestamp = timestamp;
        this.transactionProductList = transactionProductList;
        this.initiatedBy = initiatedBy;
        this.terminalId = terminalId;
    }

    public String getAccountIdValue() {
        return accountIdValue;
    }

    public void setAccountIdValue(String accountIdValue) {
        this.accountIdValue = accountIdValue;
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getTransactionTypeCode() {
        return transactionTypeCode;
    }

    public void setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
    }

    public String getTransactionUniqueReference() {
        return transactionUniqueReference;
    }

    public void setTransactionUniqueReference(String transactionUniqueReference) {
        this.transactionUniqueReference = transactionUniqueReference;
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

    public List<TransactionItem> getTransactionProductList() {
        return transactionProductList;
    }

    public void setTransactionProductList(List<TransactionItem> transactionProductList) {
        this.transactionProductList = transactionProductList;
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

    @Override
    public String toString() {
        return "TransactionRequest{" +
                "accountIdValue='" + accountIdValue + '\'' +
                ", programCode='" + programCode + '\'' +
                ", locationCode='" + locationCode + '\'' +
                ", transactionTypeCode='" + transactionTypeCode + '\'' +
                ", transactionUniqueReference='" + transactionUniqueReference + '\'' +
                ", value=" + value +
                ", timestamp=" + timestamp +
                ", transactionProductList=" + transactionProductList +
                ", initiatedBy='" + initiatedBy + '\'' +
                ", terminalId='" + terminalId + '\'' +
                '}';
    }
}
