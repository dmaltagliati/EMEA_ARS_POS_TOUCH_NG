package com.ncr.zatca.greencore.data;

import lombok.Data;

@Data
public class Item {
    private String name;
    private TaxCategory classifiedTaxCategory;
    private StandardItemIdentification standardItemIdentification;
}
