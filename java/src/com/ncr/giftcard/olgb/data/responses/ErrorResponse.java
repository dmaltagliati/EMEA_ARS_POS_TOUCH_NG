package com.ncr.giftcard.olgb.data.responses;

public class ErrorResponse extends Response implements IResponseGC {

    private boolean isErrorResponse;

    public ErrorResponse(boolean isSuccessful, String errorCode, String errorMessage, String note, boolean isErrorResponse) {
        super(isSuccessful, errorCode, errorMessage, note);
        this.isErrorResponse = isErrorResponse;
    }

    public boolean isErrorResponse() {
        return isErrorResponse;
    }

    public void setErrorResponse(boolean errorResponse) {
        isErrorResponse = errorResponse;
    }
}
