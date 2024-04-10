package com.ncr.zatca.greencore.data.b2b.sap;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class B2bError extends B2bResponse {
    @SerializedName("REASON")
    private String reason;
}
