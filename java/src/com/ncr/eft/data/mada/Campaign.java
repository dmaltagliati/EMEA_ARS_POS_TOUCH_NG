package com.ncr.eft.data.mada;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Campaign")
public class Campaign {


    private String qrCodeData = "";

    private long campaignText;

    @XmlElement(name = "QrCodeData")
    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    @XmlElement(name = "CampaignText")
    public long getCampaignText() {
        return campaignText;
    }

    public void setCampaignText(long campaignText) {
        this.campaignText = campaignText;
    }
}
