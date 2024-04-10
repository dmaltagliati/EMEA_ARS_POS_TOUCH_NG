package com.ncr.ecommerce.data;

import com.ncr.common.data.AdditionalInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Tender {

    private String type;
    private BigDecimal amount;
    private List<AdditionalInfo> additionalInfo = new ArrayList<AdditionalInfo>();  //ECOMMERCE-SUSPEND-CGA#A

    public Tender(String type, BigDecimal amount, List<AdditionalInfo> additionalInfo) {
        this.type = type;
        this.amount = amount;
        this.additionalInfo = additionalInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    //ECOMMERCE-SUSPEND-CGA#A BEG
    public List<AdditionalInfo> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(List<AdditionalInfo> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    //ECOMMERCE-SUSPEND-CGA#A END
}
