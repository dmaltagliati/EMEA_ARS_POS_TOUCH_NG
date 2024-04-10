package com.ncr.giftcard.olgb.data.requests;

public class Request {
    private String merchantId;
    private String terminalId;
    private String cashierId;
    private String note;


    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Request(String merchantId, String terminalId, String cashierId, String note) {
        this.merchantId = merchantId;
        this.terminalId = terminalId;
        this.cashierId = cashierId;
        this.note = note;
    }
}
