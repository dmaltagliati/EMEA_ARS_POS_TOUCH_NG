package com.ncr.loyalty.aym.data;

public class ErrorResponse implements LoyaltyResponseInterface {
    private InnerResponse response;

    public ErrorResponse() {
    }

    public ErrorResponse(InnerResponse response) {
        this.response = response;
    }

    public InnerResponse getResponse() {
        return response;
    }

    public void setResponse(InnerResponse response) {
        this.response = response;
    }
}
