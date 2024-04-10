package com.ncr.zatca.greencore.data.b2b;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Id {
    private String id;
    @SerializedName("schemeID")
    private String schemeId;
}
