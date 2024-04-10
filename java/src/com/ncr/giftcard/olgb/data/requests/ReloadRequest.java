package com.ncr.giftcard.olgb.data.requests;

public class ReloadRequest extends PaymentRequest {
    private String originalReceiptNo;

    public ReloadRequest(String merchantId, String terminalId, String cashierId, String note) {
        super(merchantId, terminalId, cashierId, note);
    }

    public String getOriginalReceiptNo() {
        return originalReceiptNo;
    }

    public void setOriginalReceiptNo(String originalReceiptNo) {
        this.originalReceiptNo = originalReceiptNo;
    }
}
