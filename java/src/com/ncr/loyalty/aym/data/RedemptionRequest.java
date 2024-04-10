package com.ncr.loyalty.aym.data;

import java.math.BigDecimal;
import java.util.Date;

public class RedemptionRequest extends PostRequest {
    private String accountIdValue;
    private String rewardsCode;
    private String transactionTypeCode;
    private String transactionUniqueReference;
    private BigDecimal value;
    private String locationCode;
    private Date timestamp;
    private String programCode;
    private String initiatedBy;
    private String terminalId;

    public RedemptionRequest() {
    }

    public RedemptionRequest(String accountIdValue, String rewardsCode, String transactionTypeCode, String transactionUniqueReference, BigDecimal value, String locationCode, Date timestamp, String programCode, String initiatedBy, String terminalId) {
        this.accountIdValue = accountIdValue;
        this.rewardsCode = rewardsCode;
        this.transactionTypeCode = transactionTypeCode;
        this.transactionUniqueReference = transactionUniqueReference;
        this.value = value;
        this.locationCode = locationCode;
        this.timestamp = timestamp;
        this.programCode = programCode;
        this.initiatedBy = initiatedBy;
        this.terminalId = terminalId;
    }

    public String getAccountIdValue() {
        return accountIdValue;
    }

    public void setAccountIdValue(String accountIdValue) {
        this.accountIdValue = accountIdValue;
    }

    public String getRewardsCode() {
        return rewardsCode;
    }

    public void setRewardsCode(String rewardsCode) {
        this.rewardsCode = rewardsCode;
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

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
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
        return "RedemptionRequest{" +
                "accountIdValue='" + accountIdValue + '\'' +
                ", rewardsCode='" + rewardsCode + '\'' +
                ", transactionTypeCode='" + transactionTypeCode + '\'' +
                ", transactionUniqueReference='" + transactionUniqueReference + '\'' +
                ", value=" + value +
                ", locationCode='" + locationCode + '\'' +
                ", timestamp=" + timestamp +
                ", programCode='" + programCode + '\'' +
                ", initiatedBy='" + initiatedBy + '\'' +
                ", terminalId='" + terminalId + '\'' +
                '}';
    }
}
