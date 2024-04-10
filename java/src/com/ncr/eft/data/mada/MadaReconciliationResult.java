package com.ncr.eft.data.mada;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "madaReconciliationResult")
public class MadaReconciliationResult {

    private Retailer retailer;

    private Performance performance;

    private String bankId = "";

    private String merchantID = "";

    private String terminalID = "";

    private String MCC = "";

    private String STAN = "";

    private String version = "";

    private String RRN = "";

    private CardScheme cardScheme;

    private ApplicationLabel applicationLabel;

    private String PAN = "";

    private String cardExpiryDate = "";

    private TransactionType transactionType;

    private Amounts amounts;

    private Result result;

    private CardholderVerification cardholderVerification;

    private String approvalCode = "";

    private EMV_Tags tags;

    private Campaign campaign;

    private String additionalData = "";

    @XmlElement(name = "Retailer")
    public Retailer getRetailer() {
        return retailer;
    }
    public void setRetailer(Retailer retailer) {
        this.retailer = retailer;
    }

    @XmlElement(name = "Performance")
    public Performance getPerformance() {
        return performance;
    }
    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    @XmlElement(name = "BankId")
    public String getBankId() {return bankId;}
    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    @XmlElement(name = "MerchantID")
    public String getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }

    @XmlElement(name = "TerminalID")
    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    @XmlElement(name = "MCC")
    public String getMCC() {
        return MCC;
    }

    public void setMCC(String MCC) {
        this.MCC = MCC;
    }

    @XmlElement(name = "STAN")
    public String getSTAN() {
        return STAN;
    }

    public void setSTAN(String STAN) {
        this.STAN = STAN;
    }

    @XmlElement(name = "Version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name = "RRN")
    public String getRRN() {
        return RRN;
    }

    public void setRRN(String RRN) {
        this.RRN = RRN;
    }

    @XmlElement(name = "CardScheme")
    public CardScheme getCardScheme() {
        return cardScheme;
    }

    public void setCardScheme(CardScheme cardScheme) {
        this.cardScheme = cardScheme;
    }

    @XmlElement(name = "ApplicationLabel")
    public ApplicationLabel getApplicationLabel() {
        return applicationLabel;
    }

    public void setApplicationLabel(ApplicationLabel applicationLabel) {
        this.applicationLabel = applicationLabel;
    }

    @XmlElement(name = "PAN")
    public String getPAN() {
        return PAN;
    }

    public void setPAN(String PAN) {
        this.PAN = PAN;
    }

    @XmlElement(name = "CardExpiryDate")
    public String getCardExpiryDate() {
        return cardExpiryDate;
    }

    public void setCardExpiryDate(String cardExpiryDate) {
        this.cardExpiryDate = cardExpiryDate;
    }

    @XmlElement(name = "TransactionType")
    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @XmlElement(name = "Amounts")
    public Amounts getAmounts() {
        return amounts;
    }

    public void setAmounts(Amounts amounts) {
        this.amounts = amounts;
    }

    @XmlElement(name = "Result")
    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @XmlElement(name = "CardholderVerification")
    public CardholderVerification getCardholderVerification() {
        return cardholderVerification;
    }

    public void setCardholderVerification(CardholderVerification cardholderVerification) {
        this.cardholderVerification = cardholderVerification;
    }

    @XmlElement(name = "ApprovalCode")
    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    @XmlElement(name = "EMV_Tags")
    public EMV_Tags getTags() {
        return tags;
    }

    public void setTags(EMV_Tags tags) {
        this.tags = tags;
    }

    @XmlElement(name = "Campaign")
    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    @XmlElement(name = "AdditionalData")
    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }
}
