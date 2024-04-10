package com.ncr.common.data.nextgen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item extends BasicInfo {
    private BigDecimal quantity;
    private BigDecimal amount;
}
