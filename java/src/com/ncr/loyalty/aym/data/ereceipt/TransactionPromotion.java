package com.ncr.loyalty.aym.data.ereceipt;

import java.util.Date;

public class TransactionPromotion {
    private Date timestamp;

    public TransactionPromotion() {
    }

    public TransactionPromotion(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TransactionPromotion{" +
                "timestamp=" + timestamp +
                '}';
    }
}
