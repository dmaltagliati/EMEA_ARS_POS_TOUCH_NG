package com.ncr.giftcard.olgb.data.responses;

public class Response extends ResponseBase{
    private boolean isSuccessful;

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }


    public Response() {}

    public Response(boolean isSuccessful,String errorCode, String errorMessage, String note) {
        super(errorCode, errorMessage, note);
        this.isSuccessful = isSuccessful;
    }
}
