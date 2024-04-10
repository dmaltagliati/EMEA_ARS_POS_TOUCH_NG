package com.ncr.aymcoupon.data;

import com.ncr.loyalty.transaction.LoyaltyData;

public class CouponError extends Coupon{

    private String responseCode = "";
    private String responseMessage = "";

    private boolean isSuccess;

    public CouponError(String responseCode, String responseMessage, boolean success) {
        super();
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.isSuccess = success;
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
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    @Override
    public String toString() {
        return "CouponError{" +
                "responseCode='" + responseCode + '\'' +
                ", responseMessage='" + responseMessage + '\'' +
                ", isSuccess='" + isSuccess + '\'' +
                '}';
    }
}
