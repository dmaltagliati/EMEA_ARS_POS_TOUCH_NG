package com.ncr.giftcard.olgb.data.requests;

public class ConfirmTransactionRequest extends Request {
    private String referenceNumber;

    public ConfirmTransactionRequest(String merchantId, String terminalId, String cashierId, String note) {
        super(merchantId, terminalId, cashierId, note);
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
}
