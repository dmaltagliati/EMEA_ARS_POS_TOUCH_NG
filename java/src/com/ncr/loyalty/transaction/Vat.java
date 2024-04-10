package com.ncr.loyalty.transaction;

import java.math.BigDecimal;

public class Vat {
    private BigDecimal tax;
    private BigDecimal vat;
    private BigDecimal befVat;
    private BigDecimal inclVat;

    public Vat(BigDecimal tax, BigDecimal vat, BigDecimal befVat, BigDecimal inclVat) {
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
        return "Vat{" +
                "tax=" + tax +
                ", vat=" + vat +
                ", befVat=" + befVat +
                ", inclVat=" + inclVat +
                '}';
    }
}
