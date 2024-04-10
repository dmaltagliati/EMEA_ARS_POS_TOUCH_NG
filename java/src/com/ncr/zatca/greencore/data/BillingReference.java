package com.ncr.zatca.greencore.data;

import lombok.Data;

@Data
public class BillingReference {
    private InvoiceDocumentReference invoiceDocumentReference;

    public BillingReference(String id) {
        invoiceDocumentReference = new InvoiceDocumentReference();
        invoiceDocumentReference.setId(id);
    }
}
