package com.ncr.ssco.communication.entities.pos;

public class SscoCustomer {
    private String accountNumber;
    private String countryCode;
    private String entryMethod;
    private String pointsValue;
    private int points;

    public SscoCustomer(String accountNumber, String countryCode, String entryMethod, int points) {
        this.accountNumber = accountNumber;
        this.countryCode = countryCode;
        this.entryMethod = entryMethod;
        this.points = points;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getEntryMethod() {
        return entryMethod;
    }

    public void setEntryMethod(String entryMethod) {
        this.entryMethod = entryMethod;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getPointsValue() {
        return pointsValue;
    }

    public void setPointsValue(String pointsValue) {
        this.pointsValue = pointsValue;
    }
}
