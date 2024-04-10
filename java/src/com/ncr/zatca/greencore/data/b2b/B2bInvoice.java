package com.ncr.zatca.greencore.data.b2b;

import com.ncr.zatca.greencore.data.Invoice;
import lombok.Data;

@Data
public class B2bInvoice extends Invoice {
    private AccountingBuyerParty accountingBuyerParty;
    private String uniqueTransactionId;
    private String cashierId;
    private String customerId;
}
