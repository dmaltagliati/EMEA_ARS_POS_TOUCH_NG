package com.ncr.giftcard.olgb.data.responses;

public class ConfirmTransactionResponse extends Response implements IResponseGC {
    private String referenceNumber;
    private double balance;
    private String pinCode;
    private double pointBalance;

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public double getPointBalance() {
        return pointBalance;
    }

    public void setPointBalance(double pointBalance) {
        this.pointBalance = pointBalance;
    }
}
