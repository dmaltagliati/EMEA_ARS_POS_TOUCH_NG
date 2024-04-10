package com.ncr.loyalty.aym.data;

import com.google.gson.annotations.SerializedName;

public class InnerResponse {
    private String responseCode;
    private String responseMessage;
    @SerializedName("IsSuccess")
    private boolean success;

    public InnerResponse(String responseCode, String responseMessage, boolean success) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.success = success;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
