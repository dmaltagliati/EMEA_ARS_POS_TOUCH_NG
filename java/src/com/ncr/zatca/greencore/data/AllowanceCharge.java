package com.ncr.zatca.greencore.data;

import lombok.Data;

@Data
public class AllowanceCharge {
    private boolean chargeIndicator;
    private String allowanceChargeReason;
    private String allowanceChargeReasonCode;
    private Amount amount;
    private Amount baseAmount;
}
