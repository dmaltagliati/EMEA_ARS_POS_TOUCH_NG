package com.ncr.eft.data.apex;

import com.google.gson.annotations.SerializedName;

public class VoidResponse {
    private int cardSlot;
    private String pan;
    private String issuerNAme;
    private String hostTid;
    private String hostMid;
    private String inv;
    private String batchNum;
    private String rrn;
    private String authCode;
    private String code;
    private String description;
    @SerializedName("isApproved")
    private boolean approved;
    private String jsonData;

    public VoidResponse() {
    }

    public int getCardSlot() {
        return cardSlot;
    }

    public void setCardSlot(int cardSlot) {
        this.cardSlot = cardSlot;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getIssuerNAme() {
        return issuerNAme;
    }

    public void setIssuerNAme(String issuerNAme) {
        this.issuerNAme = issuerNAme;
    }

    public String getHostTid() {
        return hostTid;
    }

    public void setHostTid(String hostTid) {
        this.hostTid = hostTid;
    }

    public String getHostMid() {
        return hostMid;
    }

    public void setHostMid(String hostMid) {
        this.hostMid = hostMid;
    }

    public String getInv() {
        return inv;
    }

    public void setInv(String inv) {
        this.inv = inv;
    }

    public String getBatchNum() {
        return batchNum;
    }

    public void setBatchNum(String batchNum) {
        this.batchNum = batchNum;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public String toString() {
        return "VoidResponse{" +
                "cardSlot=" + cardSlot +
                ", pan='" + pan + '\'' +
                ", issuerNAme='" + issuerNAme + '\'' +
                ", hostTid='" + hostTid + '\'' +
                ", hostMid='" + hostMid + '\'' +
                ", inv='" + inv + '\'' +
                ", batchNum='" + batchNum + '\'' +
                ", rrn='" + rrn + '\'' +
                ", authCode='" + authCode + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", approved=" + approved +
                ", jsonData='" + jsonData + '\'' +
                '}';
    }
}
