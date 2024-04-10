package com.ncr.zatca.greencore.data;

import lombok.Data;

@Data
public class Price {
    private Amount priceAmount;
    private AllowanceCharge allowanceCharge;
}
