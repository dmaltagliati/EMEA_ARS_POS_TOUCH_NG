package com.ncr.loyalty.transaction;

import java.math.BigDecimal;

public class Reward extends LoyaltyData {
    private String accountIdValue;
    private long rewardId;
    private String uniqueId;
    private BigDecimal amount;
    private BigDecimal pointsRedeemed;

    public Reward(String accountIdValue, long rewardId, String uniqueId, BigDecimal amount, BigDecimal pointsRedeemed) {
        this.accountIdValue = accountIdValue;
        this.rewardId = rewardId;
        this.uniqueId = uniqueId;
        this.amount = amount;
        this.pointsRedeemed = pointsRedeemed;
    }

    public String getAccountIdValue() {
        return accountIdValue;
    }

    public void setAccountIdValue(String accountIdValue) {
        this.accountIdValue = accountIdValue;
    }

    public long getRewardId() {
        return rewardId;
    }

    public void setRewardId(long rewardId) {
        this.rewardId = rewardId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPointsRedeemed() {
        return pointsRedeemed;
    }

    public void setPointsRedeemed(BigDecimal pointsRedeemed) {
        this.pointsRedeemed = pointsRedeemed;
    }

    @Override
    public String toString() {
        return "LoyaltyReward{" +
                "accountIdValue='" + accountIdValue + '\'' +
                ", rewardId=" + rewardId +
                ", uniqueId='" + uniqueId + '\'' +
                ", amount=" + amount +
                ", pointsRedeemed=" + pointsRedeemed +
                '}';
    }
}
