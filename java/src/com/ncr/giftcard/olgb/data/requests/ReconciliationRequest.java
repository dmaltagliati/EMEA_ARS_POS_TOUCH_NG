package com.ncr.giftcard.olgb.data.requests;

import java.util.ArrayList;
import java.util.List;

public class ReconciliationRequest {

    private String merchantId;
    private int businessDate;
    private List<ReconciliationRecord> reconciliationRecords;

    public ReconciliationRequest() {
        reconciliationRecords= new ArrayList<ReconciliationRecord>();
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public int getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(int businessDate) {
        this.businessDate = businessDate;
    }

    public List<ReconciliationRecord> getReconciliationRecords() {
        return reconciliationRecords;
    }

    public void setReconciliationRecords(ReconciliationRecord reconciliationRecord) {
        int ind=getLastLineCount();
        reconciliationRecord.setLineCount(ind++);
        this.reconciliationRecords.add(reconciliationRecord);
    }
    public int getLastLineCount(){
        int ind=0;
        if(getReconciliationRecords().size()>0){
            ind=getReconciliationRecords().get(getReconciliationRecords().size()-1).getLineCount();
        }
        return ind;
    }

}
