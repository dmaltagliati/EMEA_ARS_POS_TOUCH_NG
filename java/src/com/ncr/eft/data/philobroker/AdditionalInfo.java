package com.ncr.eft.data.philobroker;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalInfo {
    private String amount;
    @SerializedName("transactiondatetime")
    private String transactionDateTime;
    @SerializedName("retrievalreferencenumber")
    private String retrievalReferenceNumber;
    @SerializedName("customdata2")
    private String customData;
}
