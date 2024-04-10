package com.ncr.zatca.greencore.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode
public class TaxCategory {
    private String id;
    private BigDecimal percent;
    private TaxScheme taxScheme;
    private String taxExemptionReasonCode;
    private String taxExemptionReason;
}
