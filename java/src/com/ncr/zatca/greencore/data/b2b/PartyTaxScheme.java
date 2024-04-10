package com.ncr.zatca.greencore.data.b2b;

import com.ncr.zatca.greencore.data.TaxScheme;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PartyTaxScheme {
    private String companyId;
    private TaxScheme taxScheme;
}
