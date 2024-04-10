package com.ncr.giftcard.olgb.data.requests;

public class ActivationRequest extends TransactionRequest {
    private String email;

    public ActivationRequest(String merchantId, String terminalId, String cashierId, String note) {
        super(merchantId, terminalId, cashierId, note);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
