package com.ncr.loyalty.aym.data.ereceipt;

import com.ncr.loyalty.aym.data.PostRequest;

public class ElectronicReceiptRequest extends PostRequest {
    private ElectronicReceiptTransaction transaction;

    public ElectronicReceiptRequest() {
    }

    public ElectronicReceiptRequest(ElectronicReceiptTransaction transaction) {
        this.transaction = transaction;
    }

    public ElectronicReceiptTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(ElectronicReceiptTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public String toString() {
        return "ElectronicReceiptRequest{" +
                "transaction=" + transaction +
                '}';
    }
}
