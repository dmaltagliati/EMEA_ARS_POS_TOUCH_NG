package com.ncr.zatca.greencore.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AllowanceChargeWithTaxCategories extends AllowanceCharge {
    private List<TaxCategory> taxCategory = new ArrayList<TaxCategory>();
}
