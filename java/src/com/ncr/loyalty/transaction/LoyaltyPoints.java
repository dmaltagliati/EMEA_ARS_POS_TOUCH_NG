package com.ncr.loyalty.transaction;

import java.math.BigDecimal;

public class LoyaltyPoints {
    private BigDecimal startingPoints;
    private BigDecimal basePoints;
    private BigDecimal bonusPoints;
    private BigDecimal earnedPoints;
    private BigDecimal redeemedPoints;

    public LoyaltyPoints() {
        basePoints = BigDecimal.ZERO;
        startingPoints = BigDecimal.ZERO;
        bonusPoints = BigDecimal.ZERO;
        earnedPoints = BigDecimal.ZERO;
        redeemedPoints = BigDecimal.ZERO;
    }

    public LoyaltyPoints(BigDecimal startingPoints) {
        this.startingPoints = startingPoints;
    }

    public BigDecimal getStartingPoints() {
        return startingPoints;
    }

    public void setStartingPoints(BigDecimal startingPoints) {
        this.startingPoints = startingPoints;
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

    public BigDecimal getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(BigDecimal earnedPoints) {
        this.earnedPoints = earnedPoints;
    }

    public BigDecimal getRedeemedPoints() {
        return redeemedPoints;
    }

    public void setRedeemedPoints(BigDecimal redeemedPoints) {
        this.redeemedPoints = redeemedPoints;
    }

    @Override
    public String toString() {
        return "LoyaltyPoints{" +
                "startingPoints=" + startingPoints +
                ", basePoints=" + basePoints +
                ", bonusPoints=" + bonusPoints +
                ", earnedPoints=" + earnedPoints +
                ", redeemedPoints=" + redeemedPoints +
                '}';
    }
}
