package com.ncr.loyalty.aym.data;

import java.math.BigDecimal;
import java.util.Date;

public class RedemptionResponse {
    private long redeemRewardsId;
    private String accountIdValue;
    private String rewardsCode;
    private String programCode;
    private String transactionTypeCode;
    private String locationCode;
    private String transactionUniqueReference;
    private BigDecimal value;
    private BigDecimal pointsRedeemed;
    private int quantity;
    private Date timestamp;
    private String partNumber;
    private String typeOfGift;
    private String chassisSize;
    private String productFamily;
    private String itemsPerPack;
    private Object deliveryAddress;
    private Object memberDetails;
    private Object rewardVariables;
    private boolean status;
    private String returnCode;

    public RedemptionResponse() {
    }

    public long getRedeemRewardsId() {
        return redeemRewardsId;
    }

    public void setRedeemRewardsId(long redeemRewardsId) {
        this.redeemRewardsId = redeemRewardsId;
    }

    public String getAccountIdValue() {
        return accountIdValue;
    }

    public void setAccountIdValue(String accountIdValue) {
        this.accountIdValue = accountIdValue;
    }

    public String getRewardsCode() {
        return rewardsCode;
    }

    public void setRewardsCode(String rewardsCode) {
        this.rewardsCode = rewardsCode;
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
    }

    public String getTransactionTypeCode() {
        return transactionTypeCode;
    }

    public void setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getTransactionUniqueReference() {
        return transactionUniqueReference;
    }

    public void setTransactionUniqueReference(String transactionUniqueReference) {
        this.transactionUniqueReference = transactionUniqueReference;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getPointsRedeemed() {
        return pointsRedeemed;
    }

    public void setPointsRedeemed(BigDecimal pointsRedeemed) {
        this.pointsRedeemed = pointsRedeemed;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getTypeOfGift() {
        return typeOfGift;
    }

    public void setTypeOfGift(String typeOfGift) {
        this.typeOfGift = typeOfGift;
    }

    public String getChassisSize() {
        return chassisSize;
    }

    public void setChassisSize(String chassisSize) {
        this.chassisSize = chassisSize;
    }

    public String getProductFamily() {
        return productFamily;
    }

    public void setProductFamily(String productFamily) {
        this.productFamily = productFamily;
    }

    public String getItemsPerPack() {
        return itemsPerPack;
    }

    public void setItemsPerPack(String itemsPerPack) {
        this.itemsPerPack = itemsPerPack;
    }

    public Object getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Object deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Object getMemberDetails() {
        return memberDetails;
    }

    public void setMemberDetails(Object memberDetails) {
        this.memberDetails = memberDetails;
    }

    public Object getRewardVariables() {
        return rewardVariables;
    }

    public void setRewardVariables(Object rewardVariables) {
        this.rewardVariables = rewardVariables;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }
}
