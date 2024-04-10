package com.ncr.eft.data.philobroker;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetail {
    @SerializedName("RetrievalReferenceNumber")
    private String retrievalReferenceNumber;

    @SerializedName("Mid")
    private String mid;

    @SerializedName("AuthCode")
    private String authCode;

    @SerializedName("CardProductName")
    private String cardProductName;

    @SerializedName("TerminalIdentification")
    private String terminalIdentification;

    @SerializedName("AccountNumber")
    private String accountNumber;

    @SerializedName("TransactionAmount")
    private String transactionAmount;
}
