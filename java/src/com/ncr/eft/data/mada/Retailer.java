package com.ncr.eft.data.mada;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Retailer")
public class Retailer {


    private String retailerNameEng;

    private String retailerNameArb;

    private String address_eng_1;

    private String address_eng_2;

    private String address_arb_1;

    private String address_arb_2;

    private String download_phone;

    @XmlAttribute(name = "RetailerNameEng")
    public String getRetailerNameEng() {
        return retailerNameEng;
    }

    public void setRetailerNameEng(String retailerNameEng) {
        this.retailerNameEng = retailerNameEng;
    }

    @XmlAttribute(name = "RetailerNameArb")
    public String getRetailerNameArb() {
        return retailerNameArb;
    }

    public void setRetailerNameArb(String retailerNameArb) {
        this.retailerNameArb = retailerNameArb;
    }

    @XmlAttribute(name = "address_eng_1")
    public String getAddress_eng_1() {
        return address_eng_1;
    }

    public void setAddress_eng_1(String address_eng_1) {
        this.address_eng_1 = address_eng_1;
    }

    @XmlAttribute(name = "address_eng_2")
    public String getAddress_eng_2() {
        return address_eng_2;
    }

    public void setAddress_eng_2(String address_eng_2) {
        this.address_eng_2 = address_eng_2;
    }

    @XmlAttribute(name = "address_arb_1")
    public String getAddress_arb_1() {
        return address_arb_1;
    }

    public void setAddress_arb_1(String address_arb_1) {
        this.address_arb_1 = address_arb_1;
    }

    @XmlAttribute(name = "address_arb_2")
    public String getAddress_arb_2() {
        return address_arb_2;
    }

    public void setAddress_arb_2(String address_arb_2) {
        this.address_arb_2 = address_arb_2;
    }

    @XmlAttribute(name = "download_phone")
    public String getDownload_phone() {
        return download_phone;
    }

    public void setDownload_phone(String download_phone) {
        this.download_phone = download_phone;
    }
}
