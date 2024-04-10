package com.ncr.eft.data.mada;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TransactionRequest")
public class TransactionRequest {

    private String command = "";

    private long amount;

    private String original_RRN = "";

    private String maskedPAN = "";

    private String SubscriberNumber;

    private int printFlag;

    private String additionalData = "";

    @XmlElement(name = "Command", required = true)
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @XmlElement(name = "Amount")
    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    @XmlElement(name = "Original_RRN")
    public String getOriginal_RRN() {
        return original_RRN;
    }

    public void setOriginal_RRN(String original_RRN) {
        this.original_RRN = original_RRN;
    }

    @XmlElement(name = "MaskedPAN")
    public String getMaskedPAN() {
        return maskedPAN;
    }

    public void setMaskedPAN(String maskedPAN) {
        this.maskedPAN = maskedPAN;
    }

    @XmlElement(name = "SubscriberNumber")
    public String getSubscriberNumber() {
        return SubscriberNumber;
    }

    public void setSubscriberNumber(String subscriberNumber) {
        SubscriberNumber = subscriberNumber;
    }

    @XmlElement(name = "PrintFlag", required = true)
    public int getPrintFlag() {
        return printFlag;
    }

    public void setPrintFlag(int printFlag) {
        this.printFlag = printFlag;
    }

    @XmlElement(name = "AdditionalData")
    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }
}