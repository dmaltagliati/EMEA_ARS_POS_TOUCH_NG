package com.ncr.giftcard.olgb.data.responses;

public class ResponseBase implements IResponseGC{
    private String errorCode;
    private String errorMessage;
    private String note;


    public ResponseBase() {
    }

    public ResponseBase(String errorCode, String errorMessage, String note) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.note = note;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }


}
