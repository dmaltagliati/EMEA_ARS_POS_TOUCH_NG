package com.ncr.giftcard.olgb.data.responses;

import java.util.List;

public class RedemptionResponse extends TransactionResponse implements IResponseGC{
    private double toBeChargedAmt;
    private List<RedeemShareDet> redeemShareDet;

    public double getToBeChargedAmt() {
        return toBeChargedAmt;
    }

    public void setToBeChargedAmt(double toBeChargedAmt) {
        this.toBeChargedAmt = toBeChargedAmt;
    }

    public List<RedeemShareDet> getRedeemShareDet() {
        return redeemShareDet;
    }

    public void setRedeemShareDet(List<RedeemShareDet> redeemShareDet) {
        this.redeemShareDet = redeemShareDet;
    }
}
