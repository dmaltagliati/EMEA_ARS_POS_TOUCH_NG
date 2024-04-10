package com.ncr.loyalty.transaction;

import com.ncr.common.data.TerminalInfo;
import com.ncr.loyalty.aym.data.Variable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private String uniqueId;
    private LoyaltyCustomer loyaltyCustomer;
    private List<Item> items;
    private BigDecimal amount;
    private List<Reward> rewards;
    private TerminalInfo terminalInfo;
    private String transactionType;
    private List<Tender> tenders;
    private List<Vat> vats;
    private String eReceiptRecipient;
    private String qrCode;
    private boolean eReceiptRequested = false;
    private List<Variable> variables;

    public Transaction() {
        items = new ArrayList<Item>();
        rewards = new ArrayList<Reward>();
        tenders = new ArrayList<Tender>();
        vats = new ArrayList<Vat>();
        variables = new ArrayList<Variable>();
    }

    public Transaction(String uniqueId, LoyaltyCustomer loyaltyCustomer, TerminalInfo terminalInfo, String transactionType) {
        this.uniqueId = uniqueId;
        this.loyaltyCustomer = loyaltyCustomer;
        this.terminalInfo = terminalInfo;
        this.transactionType = transactionType;
        items = new ArrayList<Item>();
        rewards = new ArrayList<Reward>();
        tenders = new ArrayList<Tender>();
        vats = new ArrayList<Vat>();
        variables = new ArrayList<Variable>();
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public LoyaltyCustomer getLoyaltyCustomer() {
        return loyaltyCustomer;
    }

    public void setLoyaltyCustomer(LoyaltyCustomer loyaltyCustomer) {
        this.loyaltyCustomer = loyaltyCustomer;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public List<Tender> getTenders() {
        return tenders;
    }

    public void setTenders(List<Tender> tenders) {
        this.tenders = tenders;
    }

    public TerminalInfo getTerminalInfo() {
        return terminalInfo;
    }

    public void setTerminalInfo(TerminalInfo terminalInfo) {
        this.terminalInfo = terminalInfo;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getEReceiptRecipient() {
        return eReceiptRecipient;
    }

    public void setEReceiptRecipient(String eReceiptRecipient) {
        this.eReceiptRecipient = eReceiptRecipient;
    }

    public boolean isEReceiptRequested() {
        return eReceiptRequested;
    }

    public void setEReceiptRequested(boolean eReceiptRequested) {
        this.eReceiptRequested = eReceiptRequested;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public List<Vat> getVats() {
        return vats;
    }

    public void setVats(List<Vat> vats) {
        this.vats = vats;
    }

    public String geteReceiptRecipient() {
        return eReceiptRecipient;
    }

    public void seteReceiptRecipient(String eReceiptRecipient) {
        this.eReceiptRecipient = eReceiptRecipient;
    }

    public boolean iseReceiptRequested() {
        return eReceiptRequested;
    }

    public void seteReceiptRequested(boolean eReceiptRequested) {
        this.eReceiptRequested = eReceiptRequested;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "uniqueId='" + uniqueId + '\'' +
                ", loyaltyCustomer=" + loyaltyCustomer +
                ", items=" + items +
                ", amount=" + amount +
                ", rewards=" + rewards +
                ", terminalInfo=" + terminalInfo +
                ", transactionType='" + transactionType + '\'' +
                ", tenders=" + tenders +
                ", vats=" + vats +
                ", eReceiptRecipient='" + eReceiptRecipient + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", eReceiptRequested=" + eReceiptRequested +
                ", transactionVariables=" + variables +
                '}';
    }
}
