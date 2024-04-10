package com.ncr.zatca.greencore.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class TaxScheme {
    private String id;

    public static TaxScheme VAT = new TaxScheme("VAT");
}
