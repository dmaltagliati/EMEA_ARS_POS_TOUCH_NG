package com.ncr.zatca.greencore.data.status;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class StatusReply {
    private String errorDescription;
    private int returnCode;
    private String sellerName1;
    private String sellerName2;
    private String vatRegistrationNo;
    private String store;
    private String register;
    private String invoiceHash;
    private String qr;
    private Retention retention;
    private CommunicationStatus communicationStatus;
    private ResponseBody responseBody;
    private String status;
    @SerializedName("offline_timestamp")
    private String offlineTimestamp;
    private String invoiceHashHex;
    private String previousInvoiceHash;
    private int invoiceCounterValue;
    private String receiptId;
}
