package com.ncr.giftcard.olgb.data.requests;

public class ReconciliationRecord {
    private String terminalTxNo;
    private int lineCount;
    private String terminalId;
    private String cashierId;
    private String transactionNumber;
    private String referenceNumber;
    private String transactionType;
    private String genCode;
    private String cardNumber;
    private String currency;
    private double amount;
    private double Balance;
    private String handlingFree;
    private String finalStatus;

    public String getTerminalTxNo() {
        return terminalTxNo;
    }

    public void setTerminalTxNo(String terminalTxNo) {
        this.terminalTxNo = terminalTxNo;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getCashierId() {
        return cashierId;
    }

    public void setCashierId(String cashierId) {
        this.cashierId = cashierId;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getGenCode() {
        return genCode;
    }

    public void setGenCode(String genCode) {
        this.genCode = genCode;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
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
        return Balance;
    }

    public void setBalance(double balance) {
        Balance = balance;
    }

    public String getHandlingFree() {
        return handlingFree;
    }

    public void setHandlingFree(String handlingFree) {
        this.handlingFree = handlingFree;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }


}
