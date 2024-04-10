package com.ncr.zatca.greencore.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Amount {
    public static final String SAR = "SAR";
    private BigDecimal value;
    private String currencyId;

    public Amount(BigDecimal amount) {
        this.value = amount;
        this.currencyId = SAR;
    }
}
