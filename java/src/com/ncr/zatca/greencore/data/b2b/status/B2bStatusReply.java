package com.ncr.zatca.greencore.data.b2b.status;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class B2bStatusReply {
    private boolean ok;
    @SerializedName("TIMESTAMP")
    private String timestamp;
    @SerializedName("SITE")
    private String site;
    @SerializedName("LANE")
    private String lane;
    @SerializedName("RECEIPT")
    private String receipt;
    @SerializedName("CASHIER")
    private String cashier;
    @SerializedName("RESPONSE")
    private String response;
}
