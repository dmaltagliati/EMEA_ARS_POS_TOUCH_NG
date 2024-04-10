package com.ncr.eft.data.mada;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "EMV_Tags")
public class EMV_Tags {


    private String posEntryMode = "";

    private long responseCode;

    private String terminalStatusCode = "";

    private long AID;

    private String TVR = "";

    private long TSI;

    private String CVR = "";

    private long ACI;

    private String AC = "";

    @XmlElement(name = "PosEntryMode")
    public String getPosEntryMode() {
        return posEntryMode;
    }

    public void setPosEntryMode(String posEntryMode) {
        this.posEntryMode = posEntryMode;
    }

    @XmlElement(name = "ResponseCode")
    public long getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(long responseCode) {
        this.responseCode = responseCode;
    }

    @XmlElement(name = "TerminalStatusCode")
    public String getTerminalStatusCode() {
        return terminalStatusCode;
    }

    public void setTerminalStatusCode(String terminalStatusCode) {
        this.terminalStatusCode = terminalStatusCode;
    }

    @XmlElement(name = "AID")
    public long getAID() {
        return AID;
    }

    public void setAID(long AID) {
        this.AID = AID;
    }

    @XmlElement(name = "TVR")
    public String getTVR() {
        return TVR;
    }

    public void setTVR(String TVR) {
        this.TVR = TVR;
    }

    @XmlElement(name = "TSI")
    public long getTSI() {
        return TSI;
    }

    public void setTSI(long TSI) {
        this.TSI = TSI;
    }

    @XmlElement(name = "CVR")
    public String getCVR() {
        return CVR;
    }

    public void setCVR(String CVR) {
        this.CVR = CVR;
    }

    @XmlElement(name = "ACI")
    public long getACI() {
        return ACI;
    }

    public void setACI(long ACI) {
        this.ACI = ACI;
    }

    @XmlElement(name = "AC")
    public String getAC() {
        return AC;
    }

    public void setAC(String AC) {
        this.AC = AC;
    }
}
