package com.ncr.ecommerce.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpecialItem {
    String barcode;
    String description;
    String totalizer;
}
