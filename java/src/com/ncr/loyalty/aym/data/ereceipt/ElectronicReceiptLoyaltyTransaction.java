package com.ncr.loyalty.aym.data.ereceipt;

import java.math.BigDecimal;

public class ElectronicReceiptLoyaltyTransaction extends ElectronicReceiptTransaction{
    private String programCode;
    private String transactionTypeCode;
    private String transactionTypeName;
    private BigDecimal basePoints;
    private BigDecimal bonusPoints;
    private BigDecimal pointsEarned;

    public ElectronicReceiptLoyaltyTransaction() {
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
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

    @Override
    public String toString() {
        return "ElectronicReceiptLoyaltyTransaction{" +
                "programCode='" + programCode + '\'' +
                ", transactionTypeCode='" + transactionTypeCode + '\'' +
                ", transactionTypeName='" + transactionTypeName + '\'' +
                ", basePoints=" + basePoints +
                ", bonusPoints=" + bonusPoints +
                ", pointsEarned=" + pointsEarned +
                '}';
    }
}
