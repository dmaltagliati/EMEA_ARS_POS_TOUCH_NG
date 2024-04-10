package com.ncr.zatca.data;

public enum InvoiceType {
    SIMPLIFIED_INVOICE ("SIMPLIFIEDINVOICE"),
    SIMPLIFIED_CREDIT_NOTE ("SIMPLIFIEDCREDITNOTE"),
    SIMPLIFIED_DEBIT_NOTE ("SIMPLIFIEDDEBITNOTE"),
    INVOICE ("INVOICE"),
    CREDIT_NOTE ("CREDITNOTE"),
    DEBIT_NOTE ("DEBITNOTE");

    private String value;

    InvoiceType(String value) {
        this.value = value;
    }

    public boolean equals(String value) {
        return this.value.equals(value);
    }

    public String getValue() {
        return value;
    }
}
