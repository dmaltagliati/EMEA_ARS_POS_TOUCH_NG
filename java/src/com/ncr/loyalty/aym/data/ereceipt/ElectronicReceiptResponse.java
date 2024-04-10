package com.ncr.loyalty.aym.data.ereceipt;

public class ElectronicReceiptResponse {
    private String transacitonUniqueId;

    public ElectronicReceiptResponse() {
    }

    public String getTransacitonUniqueId() {
        return transacitonUniqueId;
    }

    public void setTransacitonUniqueId(String transacitonUniqueId) {
        this.transacitonUniqueId = transacitonUniqueId;
    }

    @Override
    public String toString() {
        return "ElectronicReceiptResponse{" +
                "transacitonUniqueId='" + transacitonUniqueId + '\'' +
                '}';
    }
}
