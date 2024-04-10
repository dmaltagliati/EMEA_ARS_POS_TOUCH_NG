package com.ncr.aymcoupon.data;

import com.ncr.loyalty.aym.data.PostRequest;

public class CouponUsed extends PostRequest {

    public CouponUsed(){

    }

    public CouponUsed(String accountIdValue, String couponCode, String transactionUniqueRef){
        this.AccountIdValue = accountIdValue;
        this.CouponCode = couponCode;
        this.TransactionUniqueRef = transactionUniqueRef;
    }

    private String CouponCode = "";
    private String TransactionUniqueRef = "";
    private String AccountIdValue =  "";

    public String getTransactionUniqueRef() {
        return TransactionUniqueRef;
    }

    public void setTransactionUniqueRef(String transactionUniqueRef) {
        TransactionUniqueRef = transactionUniqueRef;
    }

    public String getAccountIdValue() {
        return AccountIdValue;
    }

    public void setAccountIdValue(String accountIdValue) {
        AccountIdValue = accountIdValue;
    }

    public String getCouponCode() {
        return CouponCode;
    }

    public void setCouponCode(String couponCode) {
        CouponCode = couponCode;
    }
}
