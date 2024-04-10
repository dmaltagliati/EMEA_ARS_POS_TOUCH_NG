package com.ncr.common.data.nextgen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Vat extends BasicInfo {
    private BigDecimal amount;
    private BigDecimal gross;
    private BigDecimal rate;

    public BigDecimal getNet() {
        return gross.subtract(amount);
    }
}
