package com.ncr.zatca.data;

import com.ncr.common.data.AdditionalInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class InvoiceTender {
    private int id;
    private String type;
    private String description;
    private BigDecimal amount;
    private List<AdditionalInfo> additionalInfos = new ArrayList<AdditionalInfo>();
}
