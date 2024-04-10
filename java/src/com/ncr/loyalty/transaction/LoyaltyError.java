package com.ncr.loyalty.transaction;

public class LoyaltyError extends LoyaltyData {
    private String responseCode;
    private String responseMessage;

    public LoyaltyError(String responseCode, String responseMessage) {
        super();
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
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

    @Override
    public String toString() {
        return "LoyaltyError{" +
                "responseCode='" + responseCode + '\'' +
                ", responseMessage='" + responseMessage + '\'' +
                '}';
    }
}
