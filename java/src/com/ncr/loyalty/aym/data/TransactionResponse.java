package com.ncr.loyalty.aym.data;

import java.math.BigDecimal;
import java.util.Date;

public class TransactionResponse {
    private long transactionId;
    private long memberId;
    private String accountIdValue;
    private String programCode;
    private String locationCode;
    private String transactionTypeCode;
    private String transactionTypeName;
    private String transactionUniqueReference;
    private BigDecimal value;
    private BigDecimal basePoints;
    private BigDecimal bonusPoints;
    private BigDecimal pointsEarned;
    private Date timestamp;

    public TransactionResponse() {
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
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

    public String getTransactionTypeName() {
        return transactionTypeName;
    }

    public void setTransactionTypeName(String transactionTypeName) {
        this.transactionTypeName = transactionTypeName;
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

    public BigDecimal getBasePoints() {
        return basePoints;
    }

    public void setBasePoints(BigDecimal basePoints) {
        this.basePoints = basePoints;
    }

    public BigDecimal getBonusPoints() {
        return bonusPoints;
    }

    public void setBonusPoints(BigDecimal bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    public BigDecimal getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(BigDecimal pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
