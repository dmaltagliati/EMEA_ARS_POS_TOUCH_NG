package com.ncr.common.data.special;

public class RedemptionItem extends SpecialItem{


    public RedemptionItem(long amount, int status) {
        super(amount, status);
    }

    @Override
    public String toString() {
        return "RedemptionItem{" +
                "amount=" + this.getAmount() +
                ", status=" + this.getStatus() +
                '}';
    }
}
