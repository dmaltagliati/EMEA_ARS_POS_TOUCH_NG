package com.ncr.giftcard.olgb.data.requests;

public class RefundRequest extends PaymentRequest{
	public RefundRequest(String merchantId, String terminalId, String cashierId, String note) {
		super(merchantId, terminalId, cashierId, note);
	}
}
