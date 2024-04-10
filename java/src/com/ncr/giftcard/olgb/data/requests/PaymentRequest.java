package com.ncr.giftcard.olgb.data.requests;

public class PaymentRequest extends TransactionRequest{
    private double paymentAmount;
    private String paymentMean;

    public PaymentRequest(String merchantId, String terminalId, String cashierId, String note) {
        super(merchantId, terminalId, cashierId, note);
    }

    public double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentMean() {
        return paymentMean;
    }

    public void setPaymentMean(String paymentMean) {
        this.paymentMean = paymentMean;
    }
}
