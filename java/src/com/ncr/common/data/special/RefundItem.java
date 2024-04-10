package com.ncr.common.data.special;

public class RefundItem extends SpecialItem{

	public RefundItem(long amount, int status) {
		super(amount, status);
	}

	@Override
	public String toString() {
		return "RefundItem{" +
				"amount=" + this.getAmount() +
				", status=" + this.getStatus() +
				'}';
	}
}
