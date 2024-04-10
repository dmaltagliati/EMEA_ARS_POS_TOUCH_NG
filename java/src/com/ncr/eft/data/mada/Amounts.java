package com.ncr.eft.data.mada;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Amounts")
public class Amounts {

    private String arabicCurrency;

    private String englishCurrency;

    private String purchaseAmount;

    @XmlAttribute(name = "ArabicCurrency")
    public String getArabicCurrency() {
        return arabicCurrency;
    }

    public void setArabicCurrency(String arabicCurrency) {
        this.arabicCurrency = arabicCurrency;
    }

    @XmlAttribute(name = "EnglishCurrency")
    public String getEnglishCurrency() {
        return englishCurrency;
    }

    public void setEnglishCurrency(String englishCurrency) {
        this.englishCurrency = englishCurrency;
    }

    @XmlElement(name = "PurchaseAmount")
    public String getPurchaseAmount() { return purchaseAmount; }

    public void setPurchaseAmount(String purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }

    @XmlElement(name = "Amount")
    public String getAmount() { return purchaseAmount; }

    public void setAmount(String purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }
}
