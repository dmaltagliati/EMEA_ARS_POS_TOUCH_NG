package com.ncr.giftcard.olgb.data.requests;

public class ReversalRequest extends Request{

    private String originalMerchantID;
    private String originalTerminalID;
    private String originalCashierID;
    private String originalTransNumber;
    private String transactionNumber;
    private String reason;
    private String track2Data;
    private String cardNo;

    public ReversalRequest(String merchantId, String terminalId, String cashierId, String note) {
        super(merchantId, terminalId, cashierId, note);
    }

    public String getOriginalMerchantID() {
        return originalMerchantID;
    }

    public void setOriginalMerchantID(String originalMerchantID) {
        this.originalMerchantID = originalMerchantID;
    }

    public String getOriginalTerminalID() {
        return originalTerminalID;
    }

    public void setOriginalTerminalID(String originalTerminalID) {
        this.originalTerminalID = originalTerminalID;
    }

    public String getOriginalCashierID() {
        return originalCashierID;
    }

    public void setOriginalCashierID(String originalCashierID) {
        this.originalCashierID = originalCashierID;
    }

    public String getOriginalTransNumber() {
        return originalTransNumber;
    }

    public void setOriginalTransNumber(String originalTransNumber) {
        this.originalTransNumber = originalTransNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTrack2Data() {
        return track2Data;
    }

    public void setTrack2Data(String track2Data) {
        this.track2Data = track2Data;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
}
