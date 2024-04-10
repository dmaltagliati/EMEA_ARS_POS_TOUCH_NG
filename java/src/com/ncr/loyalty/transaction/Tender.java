package com.ncr.loyalty.transaction;

import com.ncr.common.data.AdditionalInfo;
import com.ncr.util.KeyValueCollection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Tender {
    private BigDecimal amount;
    private String auth;
    private String paymentType;
    private String cardNo;
    private List<AdditionalInfo> additionalInfos;

    public Tender(BigDecimal amount, String paymentType) {
        this.amount = amount;
        this.paymentType = paymentType;
        additionalInfos = new ArrayList<AdditionalInfo>();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public List<AdditionalInfo> getAdditionalInfos() {
        return additionalInfos;
    }

    public void setAdditionalInfos(List<AdditionalInfo> additionalInfos) {
        this.additionalInfos = additionalInfos;
    }

    @Override
    public String toString() {
        return "Tender{" +
                "amount=" + amount +
                ", auth='" + auth + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", cardNo='" + cardNo + '\'' +
                ", additionalInfos=" + additionalInfos +
                '}';
    }
}
