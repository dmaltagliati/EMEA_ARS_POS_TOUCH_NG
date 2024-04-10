package com.ncr.zatca.greencore.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Quantity {
    private BigDecimal value;
    private String unitCode;
}
