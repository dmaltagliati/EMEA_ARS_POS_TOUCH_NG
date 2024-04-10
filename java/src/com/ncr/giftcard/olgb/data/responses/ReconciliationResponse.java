package com.ncr.giftcard.olgb.data.responses;

public class ReconciliationResponse extends ResponseBase {
    private boolean isSuccess;

    public ReconciliationResponse() { }

    public ReconciliationResponse(String errorCode, String errorMessage, String note, boolean isSuccess) {
        super(errorCode, errorMessage, note);
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
