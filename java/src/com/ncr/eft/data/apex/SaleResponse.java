package com.ncr.eft.data.apex;

import com.google.gson.annotations.SerializedName;

public class SaleResponse {
    private int cardSlot;
    private String msg;
    private String tid;
    private String mid;
    private String pan;
    private String issuerName;
    private String serial;
    private String hostTid;
    private String hostMid;
    private String invoice;
    private String batchNum;
    private String rrn;
    private String authCode;
    private String clientName;
    private String amt;
    @SerializedName("isDCC")
    private boolean dcc;
    @SerializedName("dcc")
    private SaleDcc saleDcc;
    private SaleEmv emv;
    private String code;
    private  String description;
    @SerializedName("isApproved")
    private boolean approved;
    private String jsonData;

    public SaleResponse() {
    }

    public int getCardSlot() {
        return cardSlot;
    }

    public void setCardSlot(int cardSlot) {
        this.cardSlot = cardSlot;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public SaleDcc getSaleDcc() {
        return saleDcc;
    }

    public void setSaleDcc(SaleDcc saleDcc) {
        this.saleDcc = saleDcc;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
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

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public boolean getDcc() {
        return dcc;
    }

    public void setDcc(boolean dcc) {
        this.dcc = dcc;
    }

    public SaleDcc getDoc() {
        return saleDcc;
    }

    public void setDoc(SaleDcc doc) {
        this.saleDcc = doc;
    }

    public SaleEmv getEmv() {
        return emv;
    }

    public void setEmv(SaleEmv emv) {
        this.emv = emv;
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
        return "SaleResponse{" +
                "cardSlot=" + cardSlot +
                ", msg='" + msg + '\'' +
                ", pan='" + pan + '\'' +
                ", issuerName='" + issuerName + '\'' +
                ", serial='" + serial + '\'' +
                ", hostTid='" + hostTid + '\'' +
                ", hostMid='" + hostMid + '\'' +
                ", invoice='" + invoice + '\'' +
                ", batchNum='" + batchNum + '\'' +
                ", rrn='" + rrn + '\'' +
                ", authCode='" + authCode + '\'' +
                ", clientName='" + clientName + '\'' +
                ", amt='" + amt + '\'' +
                ", dcc=" + dcc +
                ", saleDcc=" + saleDcc +
                ", emv=" + emv +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", approved=" + approved +
                ", jsonData='" + jsonData + '\'' +
                '}';
    }
}
