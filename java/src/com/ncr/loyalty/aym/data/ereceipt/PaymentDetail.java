package com.ncr.loyalty.aym.data.ereceipt;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class PaymentDetail {
    private BigDecimal value;
    private String auth;
    private String paymentType;
    private String cardNo;
    @SerializedName("EFT_TRANS_NO")
    private String eftTransactionNumber;
    @SerializedName("TERMINAL_ID")
    private String terminalId;

    public PaymentDetail() {
    }

    public PaymentDetail(BigDecimal value, String paymentType) {
        this.value = value;
        this.paymentType = paymentType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getEftTransactionNumber() {
        return eftTransactionNumber;
    }

    public void setEftTransactionNumber(String eftTransactionNumber) {
        this.eftTransactionNumber = eftTransactionNumber;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    @Override
    public String toString() {
        return "PaymentDetail{" +
                "value=" + value +
                ", auth='" + auth + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", cardNo='" + cardNo + '\'' +
                ", eftTransactionNumber='" + eftTransactionNumber + '\'' +
                ", terminalId='" + terminalId + '\'' +
                '}';
    }
}
