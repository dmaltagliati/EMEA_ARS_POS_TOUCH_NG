package com.ncr.giftcard.olgb.data.requests;

public class RedemptionRequest extends PaymentRequest{
    private double pointToRedeem;

    public RedemptionRequest(String merchantId, String terminalId, String cashierId, String note) {
        super(merchantId, terminalId, cashierId, note);
    }


    public double getPointToRedeem() {
        return pointToRedeem;
    }

    public void setPointToRedeem(double pointToRedeem) {
        this.pointToRedeem = pointToRedeem;
    }
}

