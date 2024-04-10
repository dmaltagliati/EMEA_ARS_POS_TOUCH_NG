package com.ncr.zatca.greencore.data.b2b;

import com.ncr.zatca.greencore.data.InvoiceLine;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class B2bInvoiceLine extends InvoiceLine {
    private BigDecimal vat;
    private BigDecimal weight;
}
