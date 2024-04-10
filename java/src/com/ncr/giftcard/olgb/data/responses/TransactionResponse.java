package com.ncr.giftcard.olgb.data.responses;

public class TransactionResponse extends Response implements IResponseGC{
    private String referenceNumber;
    private double previousBalance;
    private String currency;
    private double amount;
    private double balance;
    private String expireDate;
    private String mobileNo;
    private String cardNumber;
    private String pinCode;
    private String cardType;
    private double previousPoints;
    private double addedPoints;
    private double pointBalance;
    private double bonusAmount;
    private double redeemedPoints;
    private double initialBalance;
    private String earliesExpiryDate;
    private double earliesExpiryAmount;

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public double getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(double previousBalance) {
        this.previousBalance = previousBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public double getPreviousPoints() {
        return previousPoints;
    }

    public void setPreviousPoints(double previousPoints) {
        this.previousPoints = previousPoints;
    }

    public double getAddedPoints() {
        return addedPoints;
    }

    public void setAddedPoints(double addedPoints) {
        this.addedPoints = addedPoints;
    }

    public double getPointBalance() {
        return pointBalance;
    }

    public void setPointBalance(double pointBalance) {
        this.pointBalance = pointBalance;
    }

    public double getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(double bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public double getRedeemedPoints() {
        return redeemedPoints;
    }

    public void setRedeemedPoints(double redeemedPoints) {
        this.redeemedPoints = redeemedPoints;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getEarliesExpiryDate() {
        return earliesExpiryDate;
    }

    public void setEarliesExpiryDate(String earliesExpiryDate) {
        this.earliesExpiryDate = earliesExpiryDate;
    }

    public double getEarliesExpiryAmount() {
        return earliesExpiryAmount;
    }

    public void setEarliesExpiryAmount(double earliesExpiryAmount) {
        this.earliesExpiryAmount = earliesExpiryAmount;
    }
}
