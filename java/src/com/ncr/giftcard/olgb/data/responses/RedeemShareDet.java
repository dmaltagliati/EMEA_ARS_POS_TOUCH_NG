package com.ncr.giftcard.olgb.data.responses;

public class RedeemShareDet  implements IResponseGC{
    private double sharedAmt;
    private int reloadSource;

    public double getSharedAmt() {
        return sharedAmt;
    }

    public void setSharedAmt(double sharedAmt) {
        this.sharedAmt = sharedAmt;
    }

    public int getReloadSource() {
        return reloadSource;
    }

    public void setReloadSource(int reloadSource) {
        this.reloadSource = reloadSource;
    }
}
