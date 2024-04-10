package com.ncr.zatca.greencore.data;

import lombok.Data;

@Data
public class MonetaryTotals {
    private Amount lineExtensionAmount;
    private Amount allowanceTotalAmount;
    private Amount chargeTotalAmount;
    private Amount taxExclusiveAmount;
    private Amount taxInclusiveAmount;
    private Amount prepaidAmount;
    private Amount payableAmount;
}
