package com.ncr.giftcard.olgb.data.responses;

public class ActivationResponse extends TransactionResponse implements IResponseGC{

    private String barcodeNumber;
    private String eGiftCardUrl;

    public String getBarcodeNumber() {
        return barcodeNumber;
    }

    public void setBarcodeNumber(String barcodeNumber) {
        this.barcodeNumber = barcodeNumber;
    }

    public String geteGiftCardUrl() {
        return eGiftCardUrl;
    }

    public void seteGiftCardUrl(String eGiftCardUrl) {
        this.eGiftCardUrl = eGiftCardUrl;
    }
}
