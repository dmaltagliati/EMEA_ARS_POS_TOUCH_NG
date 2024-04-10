package com.ncr.zatca.greencore.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaxTotalWithSubtotals extends TaxTotal {
    private List<TaxSubTotal> taxSubTotal = new ArrayList<TaxSubTotal>();
}
