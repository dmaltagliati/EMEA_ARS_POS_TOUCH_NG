package com.ncr.zatca.rbs.data;

import com.ncr.common.data.AdditionalInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class RbsInvoiceTender {
    private String type;
    private BigDecimal amount;
    private List<AdditionalInfo> additionalInfos = new ArrayList<AdditionalInfo>();
}
