package com.ncr.ecommerce.data;

import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Basket {
    public static final String SALE = "Sale";
    public static final String RETURN = "Return";
    public static final String PREPROCESS = "Preprocess";   //ECOMMERCE-SUSPEND-CGA#A

    @Since(1.0)
    private Double version;  //ECOMMERCE-SSAM#A //PORTING-SPINNEYS-ECOMMERCE-CGA#A
    @Since(1.0)
    private String basketID;
    @Since(1.3)
    private String source;
    @Since(1.0)
    private String providerID;
    @Since(1.0)
    private String status;
    @Since(1.0)
    private String type;
    @Since(1.0)
    private String customerID;
    @Since(1.3)
    private String StoreID; //ECOMMERCE-SSAM#A //PORTING-SPINNEYS-ECOMMERCE-CGA#A
    @Since(1.0)
    private String terminalID;
    @Since(1.0)
    private int earnedLoyaltyPoints;
    @Since(1.0)
    private String transactionID;
    @Since(1.0)
    private String barcodeID;
    @Since(1.0)
    private String receipt;
    @Since(1.0)
    private BigDecimal totalAmount;
    @Since(1.0)
    private List<Item> items;
    @Since(1.0)
    private List<Item> soldItems;
    @Since(1.0)
    private List<Item> notSoldItems;
    @Since(1.0)
    private String tenderId;  //TSC
    @Since(1.3)
    private List<Item> extraItems;
    @Since(1.3)
    @Until(1.5)
    private List<Tender> tenderTypes;  //SPINNEYS
    @Since(2.0)
    private List<Tender> tenders;  //ECOMMERCE-SUSPEND-CGA#A
    @Since(1.0)
    private int errorCode;

    public Basket()
    {
    }

    //public Basket(String basketID, String status, String customerID, String terminalID, String type, int earnedLoyaltyPoints, String transactionID, String barcodeID, String receipt, BigDecimal totalAmount, ArrayList<Item> items, ArrayList<Item> soldItems, ArrayList<Item> notSoldItems, ArrayList<TenderType> tenderType, /*String tenderId,*/ int errorCode) {  //PORTING-SPINNEYS-ECOMMERCE-CGA#D
    public Basket(String basketID, String status, String customerID, String terminalID, String type, int earnedLoyaltyPoints, String transactionID, String barcodeID, String receipt, BigDecimal totalAmount, ArrayList<Item> items, ArrayList<Item> soldItems, ArrayList<Item> notSoldItems, ArrayList<Tender> tenders, String tenderId, int errorCode) {  //PORTING-SPINNEYS-ECOMMERCE-CGA#A
        this.basketID = basketID;
        this.status = status;
        this.customerID = customerID;
        this.terminalID = terminalID;
        this.type = type;
        this.earnedLoyaltyPoints = earnedLoyaltyPoints;
        this.transactionID = transactionID;
        this.barcodeID = barcodeID;
        this.receipt = receipt;
        this.totalAmount = totalAmount;
        this.items = items;
        this.soldItems = soldItems;
        this.notSoldItems = notSoldItems;
        this.tenders = tenders;
        this.tenderId=tenderId;
        this.errorCode = errorCode;
    }

    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }

    public String getTenderId() {
        return tenderId;
    }

    public void setTenderId(String tenderId) {
        if(tenderId == null)
            tenderId = "";

        this.tenderId = tenderId;
    }

    public String getBasketID() {
        return basketID;
    }

    public void setBasketID(String basketID) {
        this.basketID = basketID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if(status == null)
            status = "";

        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        if(terminalID==null)
            terminalID="";
        this.terminalID = terminalID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if(type==null)
            type="";
        this.type = type;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        if(receipt==null)
            receipt="";
        this.receipt = receipt;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        if(transactionID==null)
            transactionID="";
        this.transactionID = transactionID;
    }

    public String getBarcodeID() {
        return barcodeID;
    }

    public void setBarcodeID(String barcodeID) {
        if(barcodeID==null)
            barcodeID="";
        this.barcodeID = barcodeID;
    }

    public List<Tender> getTenders() {
        return tenders;
    }

    public void setTenders(List<Tender> tenders) {
        if(tenders == null)
            tenders = null;
        this.tenders = tenders;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Item> getSoldItems() {
        return soldItems;
    }

    public void setSoldItems(List<Item> soldItems) {
        this.soldItems = soldItems;
    }

    public List<Item> getNotSoldItems() {
        return notSoldItems;
    }

    public void setNotSoldItems(List<Item> notSoldItems) {
        this.notSoldItems = notSoldItems;
    }

    public List<Item> getExtraItems() {
        return extraItems;
    }

    public void setExtraItems(List<Item> extraItems) {
        this.extraItems = extraItems;
    }

    public void update(Item item, boolean notSold) {
        if (notSold) {
            notSoldItems.add(item);
        } else {
            soldItems.add(item);
        }
    }

    public void reset() {
        notSoldItems = new ArrayList<Item>();
        soldItems = new ArrayList<Item>();
        transactionID = "";
        totalAmount = BigDecimal.valueOf(0);
        receipt = "";
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public int getEarnedLoyaltyPoints() {
        return earnedLoyaltyPoints;
    }

    public void setEarnedLoyaltyPoints(int earnedLoyaltyPoints) {
        this.earnedLoyaltyPoints = earnedLoyaltyPoints;
    }

    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }
}
