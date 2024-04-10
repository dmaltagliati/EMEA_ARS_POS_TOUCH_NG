package com.ncr.giftcard.olgb.data.requests;

public class TransactionRequest extends Request{
    private String transactionNumber;
    private String gencode;
    private String cardNumber;
    private String track2Data;
    private double amount;
    private String reason;
    private String validityDate;
    private String mobileNo;
    private double basketAmount;
    private String basketCategories;
    private String pinCode;
    private String EAN128;
    private String bin;

    public TransactionRequest(String merchantId, String terminalId, String cashierId, String note) {
        super(merchantId, terminalId, cashierId, note);
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getGencode() {
        return gencode;
    }

    public void setGencode(String gencode) {
        this.gencode = gencode;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getTrack2Data() {
        return track2Data;
    }

    public void setTrack2Data(String track2Data) {
        this.track2Data = track2Data;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getValidityDate() {
        return validityDate;
    }

    public void setValidityDate(String validityDate) {
        this.validityDate = validityDate;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public double getBasketAmount() {
        return basketAmount;
    }

    public void setBasketAmount(double basketAmount) {
        this.basketAmount = basketAmount;
    }

    public String getBasketCategories() {
        return basketCategories;
    }

    public void setBasketCategories(String basketCategories) {
        this.basketCategories = basketCategories;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getEAN128() {
        return EAN128;
    }

    public void setEAN128(String EAN128) {
        this.EAN128 = EAN128;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }
}
