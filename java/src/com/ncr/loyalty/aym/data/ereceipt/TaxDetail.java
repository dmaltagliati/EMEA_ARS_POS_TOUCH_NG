package com.ncr.loyalty.aym.data.ereceipt;

import java.math.BigDecimal;

public class TaxDetail {
    private BigDecimal tax;
    private BigDecimal vat;
    private BigDecimal befVat;
    private BigDecimal inclVat;

    public TaxDetail() {
    }

    public TaxDetail(BigDecimal tax, BigDecimal vat, BigDecimal befVat, BigDecimal inclVat) {
        this.tax = tax;
        this.vat = vat;
        this.befVat = befVat;
        this.inclVat = inclVat;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getVat() {
        return vat;
    }

    public void setVat(BigDecimal vat) {
        this.vat = vat;
    }

    public BigDecimal getBefVat() {
        return befVat;
    }

    public void setBefVat(BigDecimal befVat) {
        this.befVat = befVat;
    }

    public BigDecimal getInclVat() {
        return inclVat;
    }

    public void setInclVat(BigDecimal inclVat) {
        this.inclVat = inclVat;
    }

    @Override
    public String toString() {
        return "TaxDetail{" +
                "tax=" + tax +
                ", vat=" + vat +
                ", befVat=" + befVat +
                ", inclVat=" + inclVat +
                '}';
    }
}
