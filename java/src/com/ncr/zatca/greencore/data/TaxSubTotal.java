package com.ncr.zatca.greencore.data;

import lombok.Data;

@Data
public class TaxSubTotal {
    private Amount taxableAmount;
    private Amount taxAmount;
    private TaxCategory taxCategory;
}
